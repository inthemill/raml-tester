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
package guru.nidi.ramltester.apidesigner;

import guru.nidi.ramltester.loader.RepositoryEntry;

/**
 *
 */
class ApiPortalFile implements RepositoryEntry {
    private String path;
    private String name;
    private String type;
    private String content;
    private String api_nid;
    private String ref_rfids;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getApi_nid() {
        return api_nid;
    }

    public void setApi_nid(String api_nid) {
        this.api_nid = api_nid;
    }

    public String getRef_rfids() {
        return ref_rfids;
    }

    public void setRef_rfids(String ref_rfids) {
        this.ref_rfids = ref_rfids;
    }

    @Override
    public String toString() {
        return "ApiPortalFile{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
