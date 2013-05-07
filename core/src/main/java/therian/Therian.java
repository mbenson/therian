/*
 *  Copyright the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package therian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;

import org.apache.commons.functor.UnaryProcedure;
import org.apache.commons.functor.generator.IteratorToGeneratorAdapter;
import org.apache.commons.lang3.Validate;

import therian.Operator.DependsOn;
import therian.uelbox.ELContextWrapper;
import therian.uelbox.IterableELResolver;
import therian.uelbox.SimpleELContext;

/**
 * Therian entry point.
 */
public class Therian {
    private static final TherianModule DEFAULT_MODULE = TherianModule.create().withOperators(Operators.standard())
        .withELResolvers(new IterableELResolver());

    private static final Therian STANDARD = Therian.usingModules(DEFAULT_MODULE);
    private static Therian usingDiscoveredModules;

    private final TherianModule[] modules;
    private final List<Operator<?>> operators = new ArrayList<Operator<?>>();
    private final List<ELResolver> elResolvers = new ArrayList<ELResolver>();

    private Therian(TherianModule... modules) {
        this.modules = modules == null ? new TherianModule[0] : modules;

        final Set<Class<?>> operatorsPresent = new HashSet<Class<?>>();
        final Set<Class<?>> operatorsNeeded = new HashSet<Class<?>>();

        int moduleNumber = 0;
        for (TherianModule module : this.modules) {
            Validate.noNullElements(module.getOperators(), "null operator at index %2$s of module %1$s", moduleNumber);
            Collections.addAll(operators, module.getOperators());
            for (Operator<?> operator : module.getOperators()) {
                final Class<?> opType = operator.getClass();
                operatorsPresent.add(opType);

                Class<?> c = opType;
                while (c != null) {
                    final DependsOn dependsOn = opType.getAnnotation(DependsOn.class);
                    if (dependsOn != null) {
                        Collections.addAll(operatorsNeeded, dependsOn.value());
                    }
                    c = c.getSuperclass();
                }
            }
            Collections.addAll(elResolvers, module.getElResolvers());
            moduleNumber++;
        }
        operatorsNeeded.removeAll(operatorsPresent);
        Validate.isTrue(operatorsNeeded.isEmpty(), "Missing required operators: %s", operatorsNeeded);

        Collections.sort(operators, Operators.comparator());
        IteratorToGeneratorAdapter.adapt(operators.iterator()).run(new UnaryProcedure<Operator<?>>() {

            public void run(Operator<?> obj) {
                Operators.validateImplementation(obj);
            }
        });
    }

    Iterable<Operator<?>> getOperators() {
        return operators;
    }

    public TherianContext context() {
        return contextFor(new SimpleELContext());
    }

    public TherianContext contextFor(ELContext wrapped) {
        TherianContext result = new TherianContext(new ELContextWrapper(wrapped) {

            @Override
            protected ELResolver wrap(ELResolver elResolver) {
                final CompositeELResolver compositeResolver = new CompositeELResolver();
                for (TherianModule module : modules) {
                    for (ELResolver configuredELResolver : module.getElResolvers()) {
                        compositeResolver.add(configuredELResolver);
                    }
                }
                compositeResolver.add(elResolver);
                return compositeResolver;
            }
        });
        result.putContext(Therian.class, this);
        ExpressionFactory expressionFactory = result.getTypedContext(ExpressionFactory.class);
        if (expressionFactory == null) {
            result.putContext(ExpressionFactory.class, ExpressionFactory.newInstance());
        }
        ELContextEvent event = new ELContextEvent(result);
        for (TherianModule module : modules) {
            for (ELContextListener listener : module.getElContextListeners()) {
                listener.contextCreated(event);
            }
        }
        return result;
    }

    /**
     * Return an instance configured as {@link Therian#standard()} + {@link TherianModule}s discovered using the
     * {@link ServiceLoader} mechanism.
     *
     * @return Therian
     */
    public static synchronized Therian usingDiscoveredModules() {
        if (usingDiscoveredModules == null) {
            final List<TherianModule> modules = new ArrayList<TherianModule>();
            modules.add(DEFAULT_MODULE);
            for (TherianModule module : ServiceLoader.load(TherianModule.class)) {
                modules.add(module);
            }
            usingDiscoveredModules = new Therian(modules.toArray(new TherianModule[modules.size()]));
        }
        return usingDiscoveredModules;
    }

    public static Therian usingModules(TherianModule... modules) {
        return new Therian(modules);
    }

    /**
     * Get a Therian instance configured with standard {@link Operator}s and {@link ELResolver}s.
     *
     * @return Therian
     */
    public static Therian standard() {
        return STANDARD;
    }

}
