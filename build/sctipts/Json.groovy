import groovy.json.*
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

flowFile = session.get()
if(!flowFile) return

flowFile = session.write(flowFile, {inputStream, outputStream ->
    json = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
    def mapJson = new JsonSlurper().parseText(json)
    mapJson.table.items = new JsonBuilder(mapJson.table.items).toString()
    outputStream.write(new JsonBuilder(mapJson).toString().getBytes(StandardCharsets.UTF_8))
} as StreamCallback)
session.transfer(flowFile, REL_SUCCESS)