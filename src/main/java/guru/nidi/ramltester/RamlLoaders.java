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
package guru.nidi.ramltester;

import guru.nidi.ramltester.apidesigner.ApiRamlLoader;
import guru.nidi.ramltester.core.SchemaValidator;
import guru.nidi.ramltester.loader.*;
import org.raml.model.Raml;
import org.raml.parser.visitor.RamlDocumentBuilder;

import java.io.File;

/**
 *
 */
public class RamlLoaders {
    private final RamlLoader loader;
    private final SchemaValidators schemaValidators;

    public RamlLoaders(RamlLoader loader, SchemaValidators schemaValidators) {
        this.loader = loader;
        this.schemaValidators = schemaValidators;
    }

    public RamlLoaders(RamlLoader loader) {
        this(loader, SchemaValidators.standard());
    }

    private static RamlLoader classpathLoader(Class<?> basePackage) {
        return classpathLoader(basePackage.getPackage().getName().replace('.', '/'));
    }

    private static RamlLoader classpathLoader(String basePackage) {
        return new ClassPathRamlLoader(basePackage);
    }

    private static RamlLoader fileLoader(File baseDirectory) {
        return new FileRamlLoader(baseDirectory);
    }

    private static RamlLoader urlLoader(String baseUrl) {
        return new UrlRamlLoader(baseUrl);
    }

    private static RamlLoader apiPortalLoader(String user, String password) {
        return new ApiRamlLoader(user, password);
    }

    private static RamlLoader apiDesignerLoader(String url) {
        return new ApiRamlLoader(url);
    }

    public static RamlLoaders fromClasspath(Class<?> basePackage) {
        return using(classpathLoader(basePackage));
    }

    public static RamlLoaders fromClasspath(String basePackage) {
        return using(classpathLoader(basePackage));
    }

    public static RamlLoaders fromFile(File baseDirectory) {
        return using(fileLoader(baseDirectory));
    }

    public static RamlLoaders fromUrl(String baseUrl) {
        return using(urlLoader(baseUrl));
    }

    public static RamlLoaders fromApiPortal(String user, String password) {
        return using(apiPortalLoader(user, password));
    }

    public static RamlLoaders fromApiDesigner(String url) {
        return using(apiDesignerLoader(url));
    }

    public static RamlLoaders absolutely() {
        return using(null);
    }

    public static RamlLoaders using(RamlLoader loader) {
        return new RamlLoaders(loader);
    }


    public RamlLoaders andFromClasspath(Class<?> basePackage) {
        return andUsing(classpathLoader(basePackage));
    }

    public RamlLoaders andFromClasspath(String basePackage) {
        return andUsing(classpathLoader(basePackage));
    }

    public RamlLoaders andFromFile(File baseDirectory) {
        return andUsing(fileLoader(baseDirectory));
    }

    public RamlLoaders andFromUrl(String baseUrl) {
        return andUsing(urlLoader(baseUrl));
    }

    public RamlLoaders andFromApiPortal(String user, String password) {
        return andUsing(apiPortalLoader(user, password));
    }

    public RamlLoaders andFromApiDesigner(String url) {
        return andUsing(apiDesignerLoader(url));
    }

    public RamlLoaders andUsing(RamlLoader loader) {
        return new RamlLoaders(new CompositeRamlLoader(this.loader, loader), schemaValidators);
    }

    public RamlLoaders addSchemaValidator(SchemaValidator schemaValidator) {
        return new RamlLoaders(loader, schemaValidators.addSchemaValidator(schemaValidator));
    }

    public RamlDefinition load(String name) {
        final RamlLoader decorated = new UriRamlLoader(loader);
        final Raml raml = new RamlDocumentBuilder(new RamlLoaderRamlParserResourceLoader(decorated)).build(name);
        final SchemaValidators validators = schemaValidators.withResourceLoader(decorated);
        return new RamlDefinition(raml, validators);
    }

}
