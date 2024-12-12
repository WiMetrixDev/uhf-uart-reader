#include <jni.h>

#include <string>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <termios.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/poll.h>
#include <sys/ioctl.h>
#include <time.h>
#include <errno.h>
#include <android/log.h>
#define TAG "test"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
int set_prop(int fd)
{
    struct termios newtio;

    memset(&newtio, 0, sizeof(newtio));
    /* Set the bitrate */
    cfsetospeed(&newtio, B115200); // 57600 old and 115200 new
    cfsetispeed(&newtio, B115200); // 57600 old and 115200 new

    newtio.c_cflag |= CS8;

    /* Set the parity */
    newtio.c_cflag &= ~PARENB;

    /* Set the number of stop bits */
    newtio.c_cflag &= (~CSTOPB);

    /* Selects raw (non-canonical) input and output */
    newtio.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
    newtio.c_oflag &= ~OPOST;
    newtio.c_iflag |= IGNPAR;
    /* Ignore parity errors!!! Windows driver does so why shouldn't I? */
    /* Enable receiber, hang on close, ignore control line */
    newtio.c_cflag |= CREAD | HUPCL | CLOCAL;

    /* Read 1 byte minimun, no timeout specified */
    newtio.c_cc[VMIN] = 1;
    newtio.c_cc[VTIME] = 0;

    if (tcsetattr(fd, TCSANOW, &newtio) < 0)
        return 0;

    return 1;
}
extern "C" JNIEXPORT jstring

    JNICALL
    Java_expo_modules_uhfuartreader_RFIDReaderManager_stringFromJNI(
        JNIEnv *env,
        jobject /* this */)
{
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
int fd;

extern "C" JNIEXPORT void JNICALL
Java_expo_modules_uhfuartreader_RFIDReaderManager_close(JNIEnv *env, jobject instance)
{
    if (fd > 0)
        close(fd);
}

extern "C" JNIEXPORT jobject JNICALL
Java_expo_modules_uhfuartreader_RFIDReaderManager_openPath(JNIEnv *env, jobject instance,
                                                           jstring path_)
{
    const char *path = env->GetStringUTFChars(path_, 0);

    // TODO
    jobject mFileDescriptor;
    //    char * port ="/dev/ttyS1" ;
    fd = open(path, O_RDWR | O_NOCTTY | O_NONBLOCK | O_SYNC);
    env->ReleaseStringUTFChars(path_, path);
    if (fd == -1)
    {
        /* Throw an exception */
        return NULL;
    }
    set_prop(fd);
    usleep(10);
    tcflush(fd, TCIOFLUSH);
    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
        jmethodID iFileDescriptor = env->GetMethodID(cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = env->GetFieldID(cFileDescriptor, "descriptor", "I");
        mFileDescriptor = env->NewObject(cFileDescriptor, iFileDescriptor);
        env->SetIntField(mFileDescriptor, descriptorID, (jint)fd);
    }
    return mFileDescriptor;
}
