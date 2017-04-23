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
package therian.cdi.internal;

import therian.cdi.Mapper;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.util.ArrayList;
import java.util.Collection;

public class TherianExtension implements Extension {
    private final Collection<AnnotatedType<?>> detectedMappers = new ArrayList<>();

    void captureMapper(@Observes final ProcessAnnotatedType<?> potentialMapper) {
        final AnnotatedType<?> annotatedType = potentialMapper.getAnnotatedType();
        if (annotatedType.isAnnotationPresent(Mapper.class)) {
            detectedMappers.add(annotatedType);
        }
    }

    void addMapperBeans(@Observes final AfterBeanDiscovery abd) {
        detectedMappers.stream().forEach(at -> abd.addBean(new MapperBean(at)));
        detectedMappers.clear();
    }
}
