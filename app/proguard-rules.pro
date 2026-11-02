# Proguard rules for Smart Expense & Budget Management App
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

-keepattributes *Annotation*
-keepclassmembers class * {
    @org.json.JSONObject *;
}
