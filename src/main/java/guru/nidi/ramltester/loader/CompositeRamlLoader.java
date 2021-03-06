/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.loader;

import java.io.InputStream;

/**
 *
 */
public class CompositeRamlLoader implements RamlLoader {
    private final RamlLoader[] loaders;

    public CompositeRamlLoader(RamlLoader... loaders) {
        this.loaders = loaders;
    }

    @Override
    public InputStream fetchResource(String name) throws ResourceNotFoundException {
        for (RamlLoader loader : loaders) {
            try {
                final InputStream resource = loader.fetchResource(name);
                if (resource != null) {
                    return resource;
                }
            } catch (ResourceNotFoundException e) {
                //ignore
            }
        }
        throw new ResourceNotFoundException(name);
    }
}
