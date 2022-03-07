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
package ru.ilyshkafox.myreceipt.processors.MyReceiptModules.statistic;

import org.apache.nifi.annotation.behavior.*;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.serialization.record.Record;
import org.apache.nifi.serialization.record.type.ChoiceDataType;
import org.apache.nifi.serialization.record.util.DataTypeUtils;
import ru.ilyshkafox.myreceipt.processors.MyReceiptModules.AbstractRecordProcessor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventDriven
@SideEffectFree
@SupportsBatching
@Tags({"record", "generic", "schema", "json", "csv", "avro", "log", "logs", "freeform", "text", "statistic", "convert", "custom"})
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@WritesAttribute(attribute = "", description = "")
@CapabilityDescription("")
@RequiresInstanceClassLoading
public class StatisticSetCategory extends AbstractRecordProcessor {

    public static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder()
            .name("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(DBCPService.class)
            .build();

    public static final PropertyDescriptor NAME_ATTRIBUTE = new PropertyDescriptor.Builder()
            .name("key-name")
            .displayName("Name Key")
            .description("Имя поля, по которому будет происходить поиск")
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();


    public static final PropertyDescriptor SET_ATTRIBUTE = new PropertyDescriptor.Builder()
            .name("key-set")
            .displayName("Set Key")
            .description("Имя поля,  который надо записать результат.")
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();


    protected DBCPService dbcpService;

    @OnScheduled
    public void setup(ProcessContext context) {
        // If the query is not set, then an incoming flow file is needed. Otherwise fail the initialization
        dbcpService = context.getProperty(DBCP_SERVICE).asControllerService(DBCPService.class);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(DBCP_SERVICE);
        properties.addAll(super.getSupportedPropertyDescriptors());
        properties.add(NAME_ATTRIBUTE);
        properties.add(SET_ATTRIBUTE);
        return properties;
    }

    @Override
    protected Record process(final Record record, final FlowFile flowFile, final ProcessContext context, final long count) throws ProcessException {
        String keyName = context.getProperty(NAME_ATTRIBUTE).getValue();
        String setName = context.getProperty(SET_ATTRIBUTE).getValue();


        Map<String, Object> recordMap = DataTypeUtils.convertRecordMapToJavaMap(record.toMap(), new ChoiceDataType(record.getSchema().getDataTypes()));
        String name = record.getAsString(keyName);


        Integer category = getCategory(flowFile, name);
        getLogger().info("Для \"{}\" найден категория #{}", name, category);
        recordMap.put(setName, category);

        return DataTypeUtils.toRecord(recordMap, "StatisticSetCategoryRecord");
    }


    private Integer getCategory(final FlowFile flowFile, final String name) {
        try (final Connection con = dbcpService.getConnection(flowFile.getAttributes());
             Statement preparedStatement = con.createStatement();) {
            ResultSet resultSet = preparedStatement.executeQuery("SELECT group_id, lower(word) as word FROM statistic.group_word");
            while (resultSet.next()) {
                String word = resultSet.getString("word");
                if (name.toLowerCase().contains(word)) {
                    return resultSet.getInt("group_id");
                }
            }
            return null;
        } catch (SQLException e) {
            throw new ProcessException(e.getMessage(), e);
        }
    }
}
