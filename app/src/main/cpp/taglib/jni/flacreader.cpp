#include <jni.h>
#include <flac/flacfile.h>
#include <ogg/xiphcomment.h>

namespace {
    typedef TagLib::FLAC::PictureList::ConstIterator PictureConstIterator;
}

using namespace TagLib;
using namespace FLAC;
using namespace Ogg;

extern "C"
JNIEXPORT jobject JNICALL
Java_com_github_adrijanrogan_etiketa_jni_FlacReader_readXiphComment(JNIEnv *env, jobject instance,
                                                                    jstring filename_) {
    const char *filename = env->GetStringUTFChars(filename_, nullptr);

    FLAC::File file(filename, false, AudioProperties::Average);
    jclass clazz = env->FindClass("com/github/adrijanrogan/etiketa/jni/Metadata");
    jmethodID methodId = env->GetMethodID(
            clazz, "<init>",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;[B)V");

    std::string title;
    std::string artist;
    std::string album;
    int year = -1;
    std::string mimeType;
    jbyteArray imageData_ = nullptr;

    if (file.isValid() && file.hasXiphComment()) {
        XiphComment *tag = file.xiphComment();
        FieldListMap fieldListMap = tag->fieldListMap();

        if (tag->contains("TITLE")) {
            String string = fieldListMap["TITLE"].toString();
            title = string.to8Bit(true);
        } else {
            title = "";
        }

        if (tag->contains("ARTIST")) {
            String string = fieldListMap["ARTIST"].toString();
            artist = string.toCString(true);
        } else {
            artist = "";
        }

        if (tag->contains("ALBUM")) {
            String string = fieldListMap["ALBUM"].toString();
            album = string.toCString(true);
        } else {
            album = "";
        }

        if (tag->contains("YEAR")) {
            String string = fieldListMap["YEAR"].toString();
            year = string.toInt();
        } else if (tag->contains("DATE")) {
            String string = fieldListMap["DATE"].toString();
            year = string.toInt();
        }

        const List<Picture*>& pictureList = file.pictureList();
        if (!pictureList.isEmpty()) {
            Picture *picture = pictureList[0];
            if (picture->data().size() != 0) {
                mimeType = picture->mimeType().toCString(true);
                const char *pictureData = picture->data().data();
                const unsigned int pictureSize = picture->data().size();
                imageData_ = env->NewByteArray(pictureSize);
                env->SetByteArrayRegion(imageData_, 0, pictureSize, (const jbyte *) pictureData);
            }
        }
    }


    jstring title_ = env->NewStringUTF(title.c_str());
    jstring artist_ = env->NewStringUTF(artist.c_str());
    jstring album_ = env->NewStringUTF(album.c_str());
    jstring mimeType_ = env->NewStringUTF(mimeType.c_str());

    env->ReleaseStringUTFChars(filename_, filename);

    return env->NewObject
            (clazz, methodId, title_, artist_, album_, year, mimeType_, imageData_);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_github_adrijanrogan_etiketa_jni_FlacReader_hasXiphComment(JNIEnv *env, jobject instance,
                                                                   jstring filename_) {
    const char *filename = env->GetStringUTFChars(filename_, nullptr);

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