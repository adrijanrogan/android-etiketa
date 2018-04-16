#include <jni.h>
#include <mpegfile.h>
#include <mpeg/id3v2/id3v2tag.h>
#include <mpeg/id3v2/frames/attachedpictureframe.h>

using namespace TagLib;
using namespace MPEG;

extern "C"
JNIEXPORT jint JNICALL
Java_com_github_adrijanrogan_etiketa_jni_Mp3Writer_writeId3Tag(JNIEnv *env, jobject instance,
                                                               jstring filename_,
                                                               jstring title_, jstring artist_,
                                                               jstring album_, jint year,
                                                               jstring mimeType_,
                                                               jbyteArray imageData_) {

    const char *filename, *title, *artist, *album, *mimeType;

    filename = env->GetStringUTFChars(filename_, 0);
    MPEG::File file(filename, false, AudioProperties::Average);

    // Preverimo, ali je datoteka veljavna.
    if (file.isValid()) {
        Tag *tag = file.tag();

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

        if (file.ID3v2Tag(false) && imageData_ != NULL && mimeType_ != NULL) {
            jbyte *imageData = env->GetByteArrayElements(imageData_, 0);
            mimeType = env->GetStringUTFChars(mimeType_, 0);

            ID3v2::Tag *ID3Tag = file.ID3v2Tag(false);
            ID3Tag->removeFrames("APIC");

            ByteVector *newPictureVector = new ByteVector;
            newPictureVector->setData(reinterpret_cast<const char *>(imageData));
            auto* frame = new ID3v2::AttachedPictureFrame;
            frame->setData(*newPictureVector);
            frame->setMimeType(mimeType);
            ID3Tag->addFrame(frame);

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