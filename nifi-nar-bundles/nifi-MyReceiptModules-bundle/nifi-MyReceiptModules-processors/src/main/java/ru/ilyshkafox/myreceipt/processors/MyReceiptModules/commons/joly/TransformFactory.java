/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ilyshkafox.myreceipt.processors.MyReceiptModules.commons.joly;

import com.bazaarvoice.jolt.*;
import com.bazaarvoice.jolt.chainr.spec.ChainrEntry;
import com.bazaarvoice.jolt.exception.SpecException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransformFactory {

    public static JoltTransform getTransform(final ClassLoader classLoader,final String transformType, final Object specJson) throws Exception {

        if (transformType.equals("jolt-transform-default")) {
            return new Defaultr(specJson);
        } else if (transformType.equals("jolt-transform-shift")) {
            return new Shiftr(specJson);
        } else if (transformType.equals("jolt-transform-remove")) {
            return new Removr(specJson);
        } else if (transformType.equals("jolt-transform-card")) {
            return new CardinalityTransform(specJson);
        } else if(transformType.equals("jolt-transform-sort")){
            return new Sortr();
        } else if(transformType.equals("jolt-transform-modify-default")){
          return new Modifier.Defaultr(specJson);
        } else if(transformType.equals("jolt-transform-modify-overwrite")){
            return new Modifier.Overwritr(specJson);
        } else if(transformType.equals("jolt-transform-modify-define")){
            return new Modifier.Definr(specJson);
        } else{
            return new Chainr(getChainrJoltTransformations(classLoader,specJson));
        }

    }

    @SuppressWarnings("unchecked")
    public static JoltTransform getCustomTransform(final ClassLoader classLoader, final String customTransformType, final Object specJson) throws Exception {
        final Class clazz = classLoader.loadClass(customTransformType);
        if(SpecDriven.class.isAssignableFrom(clazz)){
            final Constructor constructor = clazz.getConstructor(Object.class);
            return (JoltTransform)constructor.newInstance(specJson);

        }else{
            return (JoltTransform)clazz.newInstance();
        }
    }


    protected static List<JoltTransform> getChainrJoltTransformations(ClassLoader classLoader, Object specJson) throws Exception{
        if(!(specJson instanceof List)) {
            throw new SpecException("JOLT Chainr expects a JSON array of objects - Malformed spec.");
        } else {

            List operations = (List)specJson;

            if(operations.isEmpty()) {
                throw new SpecException("JOLT Chainr passed an empty JSON array.");
            } else {

                ArrayList<JoltTransform> entries = new ArrayList<JoltTransform>(operations.size());

                for(Object chainrEntryObj : operations) {

                    if(!(chainrEntryObj instanceof Map)) {
                        throw new SpecException("JOLT ChainrEntry expects a JSON map - Malformed spec");
                    } else {
                        Map chainrEntryMap = (Map)chainrEntryObj;
                        String opString = (String) chainrEntryMap.get("operation");
                        String operationClassName;

                        if(opString == null) {
                            throw new SpecException("JOLT Chainr \'operation\' must implement Transform or ContextualTransform");
                        } else {

                            if(ChainrEntry.STOCK_TRANSFORMS.containsKey(opString)) {
                                operationClassName = ChainrEntry.STOCK_TRANSFORMS.get(opString);
                            } else {
                                operationClassName = opString;
                            }

                            entries.add(getCustomTransform(classLoader,operationClassName,chainrEntryMap.get("spec")));
                        }
                    }
                }

                return entries;
            }
        }

    }




}
