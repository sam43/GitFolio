# Consumer ProGuard rules for retrofit-cache library

# Keep all public classes and methods in the library
-keep public class io.sam43.retrofitcache.** { *; }

# Keep annotation classes
-keep class io.sam43.retrofitcache.annotation.** { *; }