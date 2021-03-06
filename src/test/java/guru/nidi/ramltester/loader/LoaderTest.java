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

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class LoaderTest {
    @Test
    public void classPathOk() throws IOException {
        final InputStream stream = new ClassPathRamlLoader("guru/nidi/ramltester").fetchResource("simple.raml");
        assertThat(stream.read(), not(equalTo(-1)));
    }

    @Test(expected = RamlLoader.ResourceNotFoundException.class)
    public void classPathNok() {
        new ClassPathRamlLoader("guru/nidi/ramltester").fetchResource("bla");
    }

    @Test
    public void fileOk() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/ramltester");
        assertEquals("file", resource.getProtocol());
        final InputStream stream = new FileRamlLoader(new File(resource.getPath().toString())).fetchResource("simple.raml");
        assertThat(stream.read(), not(equalTo(-1)));
    }

    @Test(expected = RamlLoader.ResourceNotFoundException.class)
    public void fileNok() {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("guru/nidi/ramltester");
        assertEquals("file", resource.getProtocol());
        new FileRamlLoader(new File(resource.getPath().toString())).fetchResource("bla");
    }

    @Test
    public void urlOk() throws IOException {
        final InputStream stream = new UrlRamlLoader("http://en.wikipedia.org/wiki").fetchResource("Short");
        assertThat(stream.read(), not(equalTo(-1)));
    }

    @Test(expected = RamlLoader.ResourceNotFoundException.class)
    public void urlNok() {
        new UrlRamlLoader("http://en.wikipedia.org").fetchResource("dfkjsdfhfs");
    }

    @Test
    public void loadFile() throws IOException {
        final InputStream inputStream = new FileRamlLoader(new File("src/test/resources/guru/nidi/ramltester")).fetchResource("simple.raml");
        assertThat(inputStream.read(), not(equalTo(-1)));
    }



}
