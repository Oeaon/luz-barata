# Las dependencias (OkHttp, WorkManager, AdMob) traen sus propias reglas.
# Los workers se crean por reflexión a partir de su nombre de clase.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
