#include <jni.h>
#include <flac/flacfile.h>
#include <ogg/xiphcomment.h>
#include <flacpicture.h>

namespace {
    typedef TagLib::FLAC::PictureList::ConstIterator PictureConstIterator;
}

using namespace TagLib;
using namespace FLAC;
using namespace Ogg;

extern "C"
JNIEXPORT jint JNICALL
Java_com_github_adrijanrogan_etiketa_jni_FlacWriter_writeXiphComment(JNIEnv *env, jobject instance,
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
    jbyte *imageData = env->GetByteArrayElements(jArray, 0);

    FLAC::File file(filenameC, false, AudioProperties::Average);

    if (file.isValid()) {
        XiphComment *tag = file.xiphComment(true);
        FieldListMap fieldListMap = tag->fieldListMap();

        tag->setTitle(titleC);
        tag->setArtist(artistC);
        tag->setAlbum(albumC);
        tag->setYear(static_cast<unsigned int>(year));

        const List<Picture*>& pictureList = file.pictureList();
        Picture *newPicture = new Picture();
        ByteVector *newPictureVector = new ByteVector;
        newPictureVector->setData(reinterpret_cast<const char *>(imageData));
        newPicture->setMimeType(mimeTypeC);
        newPicture->setData(*newPictureVector);
        pictureList[0] = newPicture;

    } else {
        return 0;
    }

    env->ReleaseStringUTFChars(filename, filenameC);
    env->ReleaseStringUTFChars(title, titleC);
    env->ReleaseStringUTFChars(artist, artistC);
    env->ReleaseStringUTFChars(album, albumC);
    env->ReleaseStringUTFChars(mimeType, mimeTypeC);
    env->ReleaseByteArrayElements(jArray, imageData, 0);
    return 1;
}