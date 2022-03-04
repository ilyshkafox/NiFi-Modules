import java.nio.charset.StandardCharsets

def session = null
def context = null
def REL_SUCCESS = null

def flowFile = session.get()
if (!flowFile) return


def qrraw = context.getProperty("qrraw").evaluateAttributeExpressions(flowFile).getValue();
def token = context.getProperty("token").evaluateAttributeExpressions(flowFile).getValue();
def body = "qr=1" +
        "&qrraw=" + URLEncoder.encode(qrraw, StandardCharsets.UTF_8) +
        "&token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)

write.write(body.getBytes(StandardCharsets.UTF_8));
write.close();

session.

session.transfer(flowFile, REL_SUCCESS)