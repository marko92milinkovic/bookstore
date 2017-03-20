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
 * Converter for {@link rs.bookstore.book.domain.Book}.
 *
 * NOTE: This class has been automatically generated from the {@link rs.bookstore.book.domain.Book} original class using Vert.x codegen.
 */
public class BookConverter {

  public static void fromJson(JsonObject json, Book obj) {
    if (json.getValue("authors") instanceof JsonArray) {
      java.util.ArrayList<rs.bookstore.book.domain.Author> list = new java.util.ArrayList<>();
      json.getJsonArray("authors").forEach( item -> {
        if (item instanceof JsonObject)
          list.add(new rs.bookstore.book.domain.Author((JsonObject)item));
      });
      obj.setAuthors(list);
    }
    if (json.getValue("bookId") instanceof Number) {
      obj.setBookId(((Number)json.getValue("bookId")).longValue());
    }
    if (json.getValue("title") instanceof String) {
      obj.setTitle((String)json.getValue("title"));
    }
  }

  public static void toJson(Book obj, JsonObject json) {
    if (obj.getAuthors() != null) {
      JsonArray array = new JsonArray();
      obj.getAuthors().forEach(item -> array.add(item.toJson()));
      json.put("authors", array);
    }
    json.put("bookId", obj.getBookId());
    if (obj.getTitle() != null) {
      json.put("title", obj.getTitle());
    }
  }
}