#include <jni.h>
#include <mpegfile.h>
#include <mpeg/id3v2/id3v2tag.h>
#include <mpeg/id3v2/frames/attachedpictureframe.h>

using namespace TagLib;
using namespace MPEG;

extern "C"
JNIEXPORT jint JNICALL
Java_com_github_adrijanrogan_etiketa_jni_Mp3Reader_hasId3Tag(JNIEnv *env, jobject instance,
                                                             jstring filename_) {
    const char *filename = env->GetStringUTFChars(filename_, 0);

    MPEG::File file(filename, false, AudioProperties::Average);
    if (file.isValid()) {
        if (file.hasID3v2Tag()) {
            return 2;
        } else if (file.hasID3v1Tag()) {
            return 1;
        } else {
            file.ID3v2Tag(true);
            return 2;
        }
    } else {
        return 0;
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_github_adrijanrogan_etiketa_jni_Mp3Reader_readId3Tag(JNIEnv *env, jobject instance,
                                                                      jstring filename_) {
    const char *filename = env->GetStringUTFChars(filename_, 0);

    MPEG::File file(filename, false, AudioProperties::Average);
    jclass clazz = env->FindClass("com/github/adrijanrogan/etiketa/jni/Metadata");
    jmethodID methodId = env->
            GetMethodID(clazz, "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;[B)V");

    std::string titleC;
    std::string artistC;
    std::string albumC;
    int year = -1;
    std::string mimeType;
    jbyteArray jArray = NULL;

    if (file.isValid()) {
        Tag *tag = file.tag();

        titleC = tag->title().toCString(true);
        artistC = tag->artist().toCString(true);
        albumC = tag->album().toCString(true);
        year = tag->year();


        if (file.ID3v2Tag(false)) {
            const TagLib::ID3v2::FrameList& list = file.ID3v2Tag()->frameListMap()["APIC"];
            if (!list.isEmpty()) {
                const auto* frame = (ID3v2::AttachedPictureFrame*)list.front();
                mimeType = frame->mimeType().toCString(true);
                ByteVector pictureData = frame->picture().data();
                char *rawData = pictureData.data();
                jArray = env->NewByteArray(pictureData.size());
                env->SetByteArrayRegion(jArray, 0, pictureData.size(), (const jbyte *) rawData);
            }
        }
    }

    jstring titleJ = env->NewStringUTF(titleC.c_str());
    jstring artistJ = env->NewStringUTF(artistC.c_str());
    jstring albumJ = env->NewStringUTF(albumC.c_str());
    jstring mimeTypeJ = env->NewStringUTF(mimeType.c_str());

    env->ReleaseStringUTFChars(filename_, filename);

    /*
     * Use ONLY Java objects here!
     */

    return env->NewObject
            (clazz, methodId, titleJ, artistJ, albumJ, year, mimeTypeJ, jArray);
}