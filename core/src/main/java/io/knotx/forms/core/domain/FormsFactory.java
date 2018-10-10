/*
 * Copyright (C) 2018 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.knotx.forms.core.domain;

import io.knotx.dataobjects.KnotContext;
import io.knotx.forms.core.FormsKnotOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;

public final class FormsFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(FormsFactory.class);

  private FormsFactory() {
    // util class
  }

  public static List<FormEntity> create(KnotContext context, FormsKnotOptions options) {
    List<FormEntity> forms = context.getFragments().stream()
        .filter(f -> f.knots().stream().anyMatch(id -> id.startsWith(
            FormConstants.FRAGMENT_KNOT_PREFIX)))
        .map(f -> FormEntity.from(f, options))
        .collect(Collectors.toList());
    if (areUnique(forms)) {
      LOGGER.error("Form identifiers are not unique [{}]", forms.stream().map(FormEntity::identifier).toArray());
      throw new IllegalStateException("Form identifiers are not unique!");
    }
    return forms;
  }

  private static boolean areUnique(List<FormEntity> forms) {
    return forms.size() != forms.stream().map(FormEntity::identifier).collect(Collectors.toSet()).size();
  }

}
