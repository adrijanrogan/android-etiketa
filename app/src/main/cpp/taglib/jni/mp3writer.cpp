#include <jni.h>
#include <mpegfile.h>
#include <mpeg/id3v2/id3v2tag.h>
#include <mpeg/id3v2/frames/attachedpictureframe.h>

using namespace TagLib;
using namespace MPEG;

extern "C"
JNIEXPORT jint JNICALL
Java_com_github_adrijanrogan_etiketa_jni_Mp3Writer_writeId3Tag(JNIEnv *env, jobject instance,
                                                               jstring filename,
                                                               jstring title, jstring artist,
                                                               jstring album, jint year,
                                                               jstring mimeType,
                                                               jbyteArray jArray) {

    const char *filenameC = env->GetStringUTFChars(filename, 0);
    const char *titleC = env->GetStringUTFChars(title, 0);
    const char *artistC = env->GetStringUTFChars(artist, 0);
    const char *albumC = env->GetStringUTFChars(album, 0);
    const char *mimeTypeC = env->GetStringUTFChars(mimeType, 0);

    jbyte *imageData = NULL;
    if (jArray != NULL) {
        imageData = env->GetByteArrayElements(jArray, 0);
    }

    MPEG::File file(filenameC, false, AudioProperties::Average);

    if (file.isValid()) {
        Tag *tag = file.tag();
        tag->setTitle(titleC);
        tag->setArtist(artistC);
        tag->setAlbum(albumC);
        tag->setYear(static_cast<unsigned int>(year));

        if (file.ID3v2Tag(false) && imageData != NULL) {
            TagLib::ID3v2::FrameList list = file.ID3v2Tag()->frameListMap()["APIC"];
            list.clear();
            ByteVector *newPictureVector = new ByteVector;
            newPictureVector->setData(reinterpret_cast<const char *>(imageData));
            auto* frame = new ID3v2::AttachedPictureFrame;
            frame->setData(*newPictureVector);
            frame->setMimeType(mimeTypeC);
            list.append(frame);
        }

        // Ne smemo pozabiti shraniti sprememb!
        file.save();

    } else {
        return 0;
    }

    env->ReleaseStringUTFChars(filename, filenameC);
    env->ReleaseStringUTFChars(title, titleC);
    env->ReleaseStringUTFChars(artist, artistC);
    env->ReleaseStringUTFChars(album, albumC);
    env->ReleaseStringUTFChars(mimeType, mimeTypeC);
    if (jArray != NULL) {
        env->ReleaseByteArrayElements(jArray, imageData, 0);
    }
    return 1;
}