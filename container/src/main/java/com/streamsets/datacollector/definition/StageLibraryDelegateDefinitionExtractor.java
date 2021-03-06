/*
 * Copyright 2018 StreamSets Inc.
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
package com.streamsets.datacollector.definition;

import com.streamsets.datacollector.config.StageLibraryDefinition;
import com.streamsets.datacollector.config.StageLibraryDelegateDefinitition;
import com.streamsets.pipeline.api.delegate.StageLibraryDelegate;
import com.streamsets.pipeline.api.delegate.StageLibraryDelegateDef;
import com.streamsets.pipeline.api.impl.ErrorMessage;
import com.streamsets.pipeline.api.impl.Utils;

import java.util.ArrayList;
import java.util.List;

public class StageLibraryDelegateDefinitionExtractor {

  private static final StageLibraryDelegateDefinitionExtractor EXTRACTOR = new StageLibraryDelegateDefinitionExtractor() {};

  public static StageLibraryDelegateDefinitionExtractor get() {
    return EXTRACTOR;
  }

  public StageLibraryDelegateDefinitition extract(
    StageLibraryDefinition libraryDef,
    Class<? extends StageLibraryDelegate> klass
  ) {
    List<ErrorMessage> errors = validate(libraryDef, klass);
    if(!errors.isEmpty()) {
      throw new IllegalArgumentException(Utils.format("Invalid stage library definition: {}", errors));
    }

    StageLibraryDelegateDef def = klass.getAnnotation(StageLibraryDelegateDef.class);
    Class exportedInterface = def.value();

    return new StageLibraryDelegateDefinitition(
      libraryDef,
      klass,
      exportedInterface,
      libraryDef.getClassLoader()
    );
  }

  private List<ErrorMessage> validate(
    StageLibraryDefinition libraryDef,
    Class<? extends StageLibraryDelegate> klass
  ) {
    List<ErrorMessage> errors = new ArrayList<>();

    StageLibraryDelegateDef def = klass.getAnnotation(StageLibraryDelegateDef.class);

    if(def == null) {
      errors.add(new ErrorMessage(DefinitionError.DEF_300, klass.getCanonicalName()));
      return errors;
    }

    if(!def.value().isAssignableFrom(klass)) {
      errors.add(new ErrorMessage(DefinitionError.DEF_500, klass.getCanonicalName(), def.value().getCanonicalName()));
    }

    if(!StageLibraryDelegate.class.isAssignableFrom(klass)) {
      errors.add(new ErrorMessage(DefinitionError.DEF_500, klass.getCanonicalName(), StageLibraryDelegate.class.getCanonicalName()));
    }

    return errors;
  }

}
