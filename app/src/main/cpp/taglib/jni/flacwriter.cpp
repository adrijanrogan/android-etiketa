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
                                                                     jstring filename_,
                                                                     jstring title_, jstring artist_,
                                                                     jstring album_, jint year,
                                                                     jstring mimeType_,
                                                                     jbyteArray imageData_) {

    const char *filename, *title, *artist, *album, *mimeType;

    filename = env->GetStringUTFChars(filename_, 0);
    FLAC::File file(filename, false, AudioProperties::Average);

    // Preverimo, ali je datoteka veljavna.
    if (file.isValid()) {
        XiphComment *tag = file.xiphComment(true);
        FieldListMap fieldListMap = tag->fieldListMap();

        // Vsak metapodatek preverimo in zapisemo posebej za lazjo kontrolo in boljso preglednost.

        if (title_ != NULL) {
            title = env->GetStringUTFChars(title_, 0);
            tag->setTitle(title);
            env->ReleaseStringUTFChars(title_, title);
        }

        if (artist_ != NULL) {
            artist = env->GetStringUTFChars(artist_, 0);
            tag->setArtist(artist);
            env->ReleaseStringUTFChars(artist_, artist);
        }

        if (album_ != NULL) {
            album = env->GetStringUTFChars(album_, 0);
            tag->setAlbum(album);
            env->ReleaseStringUTFChars(album_, album);
        }

        if (year != -1) {
            tag->setYear(static_cast<unsigned int>(year));
        }

        if (imageData_ != NULL && mimeType_ != NULL) {
            jbyte *imageData = env->GetByteArrayElements(imageData_, 0);
            mimeType = env->GetStringUTFChars(mimeType_, 0);

            Picture *newPicture = new Picture();
            ByteVector *newPictureVector = new ByteVector;
            newPictureVector->setData(reinterpret_cast<const char *>(imageData));
            newPicture->setMimeType(mimeType);
            newPicture->setData(*newPictureVector);
            file.removePictures();
            file.addPicture(newPicture);

            env->ReleaseStringUTFChars(mimeType_, mimeType);
            env->ReleaseByteArrayElements(imageData_, imageData, 0);
        }

        // Ne smemo pozabiti shraniti sprememb!
        file.save();
        return 1;

    } else {
        return 0;
    }
}