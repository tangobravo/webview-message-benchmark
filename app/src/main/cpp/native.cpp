#include <jni.h>
#include <vector>

static const unsigned int EXTRA_CHARS = 500000;
static std::vector<jchar> randomChars;

void ensureRandomChars(unsigned int length) {
    if(randomChars.size() >= length) return;
    unsigned int oldLength = randomChars.size();
    randomChars.resize(length);
    for(unsigned int i = oldLength; i < length; ++i) {
        randomChars[i] = static_cast<jchar>(random());
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_tangobravo_webviewmessagebenchmark_BinaryMessageCallback_fillRandomString(JNIEnv *env,
                                                                                   jobject thiz,
                                                                                   jint length) {
    if(length <= 0) return NULL;
    ensureRandomChars(static_cast<unsigned int>(length) + EXTRA_CHARS);

    unsigned int startOffset = random() % EXTRA_CHARS;
    jstring str = env->NewString(&(randomChars[startOffset]), length);
    return str;
}
