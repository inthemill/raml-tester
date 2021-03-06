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
package guru.nidi.ramltester.core;

import java.util.*;

/**
 *
 */
public class Usage implements Iterable<Map.Entry<String, Usage.Resource>> {
    private Map<String, Resource> resources = new HashMap<>();

    private static <T> T getOrCreate(Class<T> clazz, Map<String, T> map, String name) {
        T res = map.get(name);
        if (res == null) {
            try {
                res = clazz.newInstance();
                map.put(name, res);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return res;
    }

    public Resource resource(String path) {
        return getOrCreate(Resource.class, resources, path);
    }

    public void add(Usage usage) {
        for (Map.Entry<String, Resource> resourceEntry : usage) {
            final Resource resource = resource(resourceEntry.getKey());
            resource.incUses(resourceEntry.getValue().getUses());
            for (Map.Entry<String, Action> actionEntry : resourceEntry.getValue()) {
                final Action action = resource.action(actionEntry.getKey());
                final Action usageAction = actionEntry.getValue();
                action.incUses(usageAction.getUses());
                action.addQueryParameters(usageAction.getQueryParameters());
                action.addRequestHeaders(usageAction.getRequestHeaders());
                action.addResponseCodes(usageAction.getResponseCodes());
                for (Map.Entry<String, Response> responseEntry : usageAction.responses()) {
                    final Response response = action.response(responseEntry.getKey());
                    response.addResponseHeaders(responseEntry.getValue().getResponseHeaders());
                }
                for (Map.Entry<String, MimeType> mimeTypeEntry : usageAction.mimeTypes()) {
                    final MimeType mimeType = action.mimeType(mimeTypeEntry.getKey());
                    mimeType.addFormParameters(mimeTypeEntry.getValue().getFormParameters());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Usage" + resources;
    }

    @Override
    public Iterator<Map.Entry<String, Resource>> iterator() {
        return resources.entrySet().iterator();
    }

    public Set<String> getUnusedResources() {
        final Set<String> res = new HashSet<>();
        for (Map.Entry<String, Resource> resourceEntry : this) {
            if (resourceEntry.getValue().getUses() == 0) {
                res.add(resourceEntry.getKey());
            }
        }
        return res;
    }

    private interface ActionCollector {
        void collect(String key, Action action, Set<String> result);
    }

    private Set<String> collect(ActionCollector actionCollector) {
        final Set<String> res = new HashSet<>();
        for (Map.Entry<String, Resource> resourceEntry : this) {
            for (Map.Entry<String, Action> actionEntry : resourceEntry.getValue()) {
                actionCollector.collect(actionEntry.getKey() + " " + resourceEntry.getKey(), actionEntry.getValue(), res);
            }
        }
        return res;
    }

    public Set<String> getUnusedActions() {
        return collect(new ActionCollector() {
            @Override
            public void collect(String key, Action action, Set<String> result) {
                if (action.getUses() == 0) {
                    result.add(key);
                }
            }
        });
    }

    public Set<String> getUnusedQueryParameters() {
        return collect(new ActionCollector() {
            @Override
            public void collect(String key, Action action, Set<String> result) {
                for (Map.Entry<String, Integer> queryEntry : action.getQueryParameters().values()) {
                    if (queryEntry.getValue() == 0) {
                        result.add(queryEntry.getKey() + " in " + key);
                    }
                }
            }
        });
    }


    public Set<String> getUnusedFormParameters() {
        return collect(new ActionCollector() {
            @Override
            public void collect(String key, Action action, Set<String> result) {
                for (Map.Entry<String, MimeType> mimeTypeEntry : action.mimeTypes()) {
                    for (Map.Entry<String, Integer> formEntry : mimeTypeEntry.getValue().getFormParameters().values()) {
                        if (formEntry.getValue() == 0) {
                            result.add(formEntry.getKey() + " in " + key + " (" + mimeTypeEntry.getKey() + ")");
                        }
                    }
                }
            }
        });
    }

    public Set<String> getUnusedRequestHeaders() {
        return collect(new ActionCollector() {
            @Override
            public void collect(String key, Action action, Set<String> result) {
                for (Map.Entry<String, Integer> requestEntry : action.getRequestHeaders().values()) {
                    if (requestEntry.getValue() == 0) {
                        result.add(requestEntry.getKey() + " in " + key);
                    }
                }
            }
        });
    }

    public Set<String> getUnusedResponseHeaders() {
        return collect(new ActionCollector() {
            @Override
            public void collect(String key, Action action, Set<String> result) {
                for (Map.Entry<String, Response> responseEntry : action.responses()) {
                    for (Map.Entry<String, Integer> headerEntry : responseEntry.getValue().getResponseHeaders().values()) {
                        if (headerEntry.getValue() == 0) {
                            result.add(headerEntry.getKey() + " in " + key + " -> " + responseEntry.getKey());
                        }
                    }
                }
            }
        });
    }

    public Set<String> getUnusedResponseCodes() {
        return collect(new ActionCollector() {
            @Override
            public void collect(String key, Action action, Set<String> result) {
                for (Map.Entry<String, Integer> responseCodeEntry : action.getResponseCodes().values()) {
                    if (responseCodeEntry.getValue() == 0) {
                        result.add(responseCodeEntry.getKey() + " in " + key);
                    }
                }
            }
        });
    }


    static class UsageBase {
        private int uses;

        public void incUses(int count) {
            uses += count;
        }

        public int getUses() {
            return uses;
        }
    }

    static class Resource extends UsageBase implements Iterable<Map.Entry<String, Action>> {
        private Map<String, Action> actions = new HashMap<>();

        public Action action(String name) {
            return getOrCreate(Action.class, actions, name);
        }

        @Override
        public String toString() {
            return "Resource" + actions;
        }

        @Override
        public Iterator<Map.Entry<String, Action>> iterator() {
            return actions.entrySet().iterator();
        }
    }

    static class Action extends UsageBase {
        private Map<String, Response> responses = new HashMap<>();
        private Map<String, MimeType> mimeTypes = new HashMap<>();
        private CountSet<String> queryParameters = new CountSet<>();
        private CountSet<String> requestHeaders = new CountSet<>();
        private CountSet<String> responseCodes = new CountSet<>();

        public Response response(String name) {
            return getOrCreate(Response.class, responses, name);
        }

        public Iterable<Map.Entry<String, Response>> responses() {
            return responses.entrySet();
        }

        public MimeType mimeType(String name) {
            return getOrCreate(MimeType.class, mimeTypes, name);
        }

        public Iterable<Map.Entry<String, MimeType>> mimeTypes() {
            return mimeTypes.entrySet();
        }

        public void addQueryParameters(Set<String> names) {
            queryParameters.addAll(names);
        }

        public void initQueryParameters(Set<String> names) {
            queryParameters.addAll(names, 0);
        }

        public void addRequestHeaders(Set<String> names) {
            requestHeaders.addAll(names);
        }

        public void initRequestHeaders(Set<String> names) {
            requestHeaders.addAll(names, 0);
        }

        public void addResponseCode(String name) {
            responseCodes.add(name);
        }

        public void addResponseCodes(Set<String> names) {
            responseCodes.addAll(names);
        }

        public void initResponseCodes(Set<String> names) {
            responseCodes.addAll(names, 0);
        }

        public CountSet<String> getQueryParameters() {
            return queryParameters;
        }

        public CountSet<String> getRequestHeaders() {
            return requestHeaders;
        }

        public CountSet<String> getResponseCodes() {
            return responseCodes;
        }

        @Override
        public String toString() {
            return "Action{" +
                    "responses=" + responses +
                    ", mimeTypes=" + mimeTypes +
                    ", queryParameters=" + queryParameters +
                    ", requestHeaders=" + requestHeaders +
                    ", responseCodes=" + responseCodes +
                    '}';
        }
    }

    static class Response {
        private CountSet<String> responseHeaders = new CountSet<>();

        public void addResponseHeaders(Set<String> names) {
            responseHeaders.addAll(names);
        }

        public void initResponseHeaders(Set<String> names) {
            responseHeaders.addAll(names, 0);
        }

        public CountSet<String> getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "responseHeaders=" + responseHeaders +
                    '}';
        }
    }

    static class MimeType {
        private CountSet<String> formParameters = new CountSet<>();

        public void addFormParameters(Set<String> names) {
            formParameters.addAll(names);
        }

        public void initFormParameters(Set<String> names) {
            formParameters.addAll(names, 0);
        }

        public CountSet<String> getFormParameters() {
            return formParameters;
        }

        @Override
        public String toString() {
            return "MimeType{" +
                    "formParameters=" + formParameters +
                    '}';
        }
    }
}
