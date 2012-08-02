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
package net.morph;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.el.ExpressionFactory;

import uelbox.SimpleELContext;

/**
 * Morph entry point.
 */
public class Morph {
    private static final MorphModule DEFAULT_MODULE = MorphModule.create().withOperators(Operators.standard());

    private static final Morph STANDARD = Morph.usingModules(DEFAULT_MODULE);

    private final MorphModule[] modules;
    //TODO sort by type/params
    private final Set<Operator<?>> operators = new LinkedHashSet<Operator<?>>();

    private Morph(MorphModule... modules) {
        this.modules = modules == null ? new MorphModule[0] : modules;
        for (MorphModule module : this.modules) {
            Collections.addAll(operators, module.getOperators());
        }
    }

    Set<Operator<?>> getOperators() {
        return operators;
    }

    public MorphContext context() {
        return contextFor(new SimpleELContext());
    }

    public MorphContext contextFor(ELContext wrapped) {
        MorphContext result = new MorphContext(wrapped);
        result.putContext(Morph.class, this);
        ExpressionFactory expressionFactory = result.getTypedContext(ExpressionFactory.class);
        if (expressionFactory == null) {
            result.putContext(ExpressionFactory.class, ExpressionFactory.newInstance());
        }
        ELContextEvent event = new ELContextEvent(result);
        for (MorphModule module : modules) {
            for (ELContextListener listener : module.getElContextListeners()) {
                listener.contextCreated(event);
            }
        }
        return result;
    }

    public static Morph usingModules(MorphModule... modules) {
        return new Morph(modules);
    }

    public static Morph standard() {
        return STANDARD;
    }

}
