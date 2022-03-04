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
package ru.ilyshkafox.myreceipt.processors.MyReceiptModules;

import lombok.Getter;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.schema.access.SchemaNotFoundException;
import org.apache.nifi.serialization.RecordSetWriterFactory;
import org.apache.nifi.serialization.SimpleRecordSchema;
import org.apache.nifi.serialization.WriteResult;
import org.apache.nifi.serialization.record.Record;
import org.apache.nifi.serialization.record.RecordField;
import org.apache.nifi.serialization.record.RecordFieldType;
import org.apache.nifi.serialization.record.RecordSchema;
import org.apache.nifi.serialization.record.util.DataTypeUtils;
import ru.ilyshkafox.myreceipt.processors.MyReceiptModules.qrreceiptparse.dao.QrCodeReceipt;
import ru.ilyshkafox.myreceipt.processors.MyReceiptModules.qrreceiptparse.services.QrCodeReceiptDecodeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.*;

@Tags({"example"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({
        @WritesAttribute(attribute = "receipt.uuid", description = "Сгенерированный индетификатор записи"),
})
public class QrReceiptParse extends AbstractProcessor {
    private static final QrCodeReceiptDecodeService service = QrCodeReceiptDecodeService.getInstance();
    private static final RecordSchema schema = new SimpleRecordSchema(
            List.of(
                    new RecordField("uuid", RecordFieldType.STRING.getDataType()),
                    new RecordField("qr_string", RecordFieldType.STRING.getDataType()),
                    new RecordField("time", RecordFieldType.TIMESTAMP.getDataType("dd.MM.yyyy hh:mm:ss")),
                    new RecordField("sum", RecordFieldType.LONG.getDataType()),
                    new RecordField("fiscal_number", RecordFieldType.LONG.getDataType()),
                    new RecordField("fiscal_document", RecordFieldType.LONG.getDataType()),
                    new RecordField("fiscal_feature", RecordFieldType.LONG.getDataType()),
                    new RecordField("create_at", RecordFieldType.TIMESTAMP.getDataType("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
            )
    );

    static final PropertyDescriptor RECORD_WRITER = new PropertyDescriptor.Builder()
            .name("record-writer")
            .displayName("Record Writer")
            .description("Specifies the Controller Service to use for writing out the records")
            .identifiesControllerService(RecordSetWriterFactory.class)
            .required(true)
            .build();

    public static final PropertyDescriptor QR_STRING = new PropertyDescriptor.Builder()
            .name("qr-string")
            .displayName("QR String")
            .description("Декодированная информация QRCode чека")
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.FLOWFILE_ATTRIBUTES)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Удачный парсинг")
            .build();

    public static final Relationship ERROR = new Relationship.Builder()
            .name("error")
            .description("Ошибка при парсинге")
            .build();

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = List.of(RECORD_WRITER, QR_STRING);

    @Getter
    private final Set<Relationship> relationships = Set.of(SUCCESS, ERROR);


    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }
        try {
            flowFile = onTrigger0(context, session, flowFile);
            flowFile = session.putAttribute(flowFile, "mime.type", "application/json");
            session.transfer(flowFile, SUCCESS);
        } catch (Exception ex) {
            getLogger().error("Ошибка при парсинге!", ex);
            session.transfer(flowFile, ERROR);
        }


    }


    private FlowFile onTrigger0(final ProcessContext context, final ProcessSession session, FlowFile flowFile) throws SchemaNotFoundException, IOException {
        final RecordSetWriterFactory writerFactory = context.getProperty(RECORD_WRITER).asControllerService(RecordSetWriterFactory.class);
        final RecordSchema writeSchema = writerFactory.getSchema(flowFile.getAttributes(), schema);

        var uuid = UUID.randomUUID();
        var qrString = context.getProperty(QR_STRING).evaluateAttributeExpressions(flowFile).getValue();
        QrCodeReceipt decode = service.decode(qrString);

        Record record = DataTypeUtils.toRecord(Map.of(
                "uuid", uuid,
                "qr_string", qrString,
                "time", Timestamp.valueOf(decode.getTime()),
                "sum", decode.getSum().multiply(BigDecimal.valueOf(100)).longValue(),
                "fiscal_number", decode.getFiscalNumber(),
                "fiscal_document", decode.getFiscalDocument(),
                "fiscal_feature", decode.getFiscalFeature(),
                "create_at", Timestamp.from(OffsetDateTime.now().toInstant())
        ), "Receipt");

        String mimeType;
        try (final var outputStream = session.write(flowFile);
             final var resultSetWriter = writerFactory.createWriter(getLogger(), writeSchema, outputStream, Collections.emptyMap())
        ) {
           resultSetWriter.write(record);
            mimeType = resultSetWriter.getMimeType();
        }

        flowFile = session.putAttribute(flowFile, CoreAttributes.MIME_TYPE.key(), mimeType);
        flowFile = session.putAttribute(flowFile, "receipt.uuid", uuid.toString());

        return flowFile;
    }


}
