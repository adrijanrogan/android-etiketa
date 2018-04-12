#include <jni.h>

JNIEXPORT jint JNICALL
Java_com_github_adrijanrogan_etiketa_jni_Mp3Reader_getFieldCount(JNIEnv *env, jobject instance,
                                                              jstring filename_) {
    const char *filename = env->GetStringUTFChars(filename_, 0);

    // TODO

    env->ReleaseStringUTFChars(filename_, filename);
}

JNIEXPORT jobject JNICALL
Java_com_github_adrijanrogan_etiketa_jni_Mp3Reader_readId3Tag(JNIEnv *env, jobject instance,
                                                                      jstring filename_) {
    const char *filename = env->GetStringUTFChars(filename_, 0);

    // TODO

    env->ReleaseStringUTFChars(filename_, filename);
}