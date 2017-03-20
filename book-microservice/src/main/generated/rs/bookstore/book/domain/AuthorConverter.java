/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package rs.bookstore.book.domain;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

/**
 * Converter for {@link rs.bookstore.book.domain.Author}.
 *
 * NOTE: This class has been automatically generated from the {@link rs.bookstore.book.domain.Author} original class using Vert.x codegen.
 */
public class AuthorConverter {

  public static void fromJson(JsonObject json, Author obj) {
    if (json.getValue("books") instanceof JsonArray) {
      java.util.ArrayList<rs.bookstore.book.domain.Book> list = new java.util.ArrayList<>();
      json.getJsonArray("books").forEach( item -> {
        if (item instanceof JsonObject)
          list.add(new rs.bookstore.book.domain.Book((JsonObject)item));
      });
      obj.setBooks(list);
    }
    if (json.getValue("name") instanceof String) {
      obj.setName((String)json.getValue("name"));
    }
  }

  public static void toJson(Author obj, JsonObject json) {
    if (obj.getBooks() != null) {
      JsonArray array = new JsonArray();
      obj.getBooks().forEach(item -> array.add(item.toJson()));
      json.put("books", array);
    }
    if (obj.getName() != null) {
      json.put("name", obj.getName());
    }
  }
}