#include <jni.h>
#include <flac/flacfile.h>
#include <ogg/xiphcomment.h>

namespace {
    typedef TagLib::FLAC::PictureList::ConstIterator PictureConstIterator;
}

using namespace TagLib;
using namespace FLAC;
using namespace Ogg;

JNIEXPORT jobject JNICALL
Java_com_github_adrijanrogan_etiketa_jni_FlacReader_readXiphComment(JNIEnv *env, jobject instance,
                                                                    jstring filename_) {
    const char *filename = env->GetStringUTFChars(filename_, 0);

    FLAC::File file(filename, false, AudioProperties::Average);
    jclass clazz = env->FindClass("com/github/adrijanrogan/etiketa/jni/Metadata");
    jmethodID methodId = env->
            GetMethodID(clazz, "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[BLjava/lang/String;)V");

    std::string titleC;
    std::string artistC;
    std::string albumC;
    int year = -1;
    std::string mimeType;
    jbyteArray jArray = NULL;

    if (file.isValid() && file.hasXiphComment()) {
        XiphComment *tag = file.xiphComment();
        FieldListMap fieldListMap = tag->fieldListMap();

        if (tag->contains("TITLE")) {
            String string = fieldListMap["TITLE"].toString();
            titleC = string.to8Bit(true);
        } else {
            titleC = "<unknown>";
        }

        if (tag->contains("ARTIST")) {
            String string = fieldListMap["ARTIST"].toString();
            artistC = string.toCString(true);
        } else {
            artistC = "<unknown>";
        }

        if (tag->contains("ALBUM")) {
            String string = fieldListMap["ALBUM"].toString();
            albumC = string.toCString(true);
        } else {
            albumC = "<unknown>";
        }

        if (tag->contains("YEAR")) {
            String string = fieldListMap["YEAR"].toString();
            year = string.toInt();
        } else if (tag->contains("DATE")) {
            String string = fieldListMap["DATE"].toString();
            year = string.toInt();
        }

        List<Picture *> pictureList = tag->pictureList();
        if (!pictureList.isEmpty()) {
            Picture *picture = pictureList[0];
            if (picture->data().size() != 0) {
                mimeType = picture->mimeType().toCString(true);
                ByteVector pictureData = picture->data();
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

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_github_adrijanrogan_etiketa_jni_FlacReader_hasXiphComment__Ljava_lang_String_2(JNIEnv *env,
                                                                                        jobject instance,
                                                                                        jstring filename_) {
    const char *filename = env->GetStringUTFChars(filename_, 0);

    FLAC::File file(filename, false, AudioProperties::Average);
    if (file.isValid()) {
        if (file.hasXiphComment()) {
            return static_cast<jboolean>(true);
        } else {
            file.xiphComment(true);
            return static_cast<jboolean>(true);
        }
    } else {
        return static_cast<jboolean>(false);
    }
}