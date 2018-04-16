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
    jclass class_ = env->FindClass("com/github/adrijanrogan/etiketa/jni/Metadata");
    jmethodID methodId_ = env->
            GetMethodID(class_, "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;[B)V");

    std::string title;
    std::string artist;
    std::string album;
    int year = -1;
    std::string mimeType;
    jbyteArray imageData_ = NULL;

    if (file.isValid()) {
        Tag *tag = file.tag();

        title = tag->title().toCString(true);
        artist = tag->artist().toCString(true);
        album = tag->album().toCString(true);
        year = tag->year();


        if (file.ID3v2Tag(false)) {
            const TagLib::ID3v2::FrameList& list = file.ID3v2Tag()->frameListMap()["APIC"];
            if (!list.isEmpty()) {
                ID3v2::AttachedPictureFrame* apic = dynamic_cast<ID3v2::AttachedPictureFrame*>
                    (list.front());
                mimeType = apic->mimeType().toCString(true);
                const ByteVector picture = apic->picture();
                const char *pictureData = picture.data();
                const unsigned pictureSize = picture.size();
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
            (class_, methodId_, title_, artist_, album_, year, mimeType_, imageData_);
}