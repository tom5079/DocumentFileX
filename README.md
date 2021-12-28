# DocumentFileX
java.io.File compatible SAF implementation

Tired of SAF bullshits? Implement SAF with ease!

### Features
 - Interoperable with java.io.File with some exceptions
 - File.getName() without overhead
 - Faster Directory IO
 - Get child FileX without overhead
 - _Slightly_ improved listFiles() //√evil
 - Automatic URI type detection
 - File metadata caching
 - Useful URI extensions
 - (WIP) kotlin-stdlib File extension compatible extension methods

## Setup
```gradle
dependencies {
    implementation 'xyz.quaver:documentfilex:0.7.1'
}
```

## Invalid Characters

Android Scoped Storage Framework doesn't allow following characters and automatically converts them to underscore(\_) for some bizarre reason.  
Filter out these characters before creating any file or folder!
Invalid characters are: `<"> <*> <:> <<> <>> <?> <\> <|>` and `</>`

## Sample Code

### File I/O
> :warning: **You have to use File I/O methods declared in `xyz.quaver.io.util.*`  
Default kotlin extension methods do not work with SAFileX Instances.**
```kotlin
val file = FileX(context, uri)

file.writeText("Hello, SAF! You're dumb.")
val text = file.readText() // "Hello, SAF! You're dumb."

val data = listOf(0x82, 0x72, 0x82, 0x60, 0x82, 0x65, 0x82, 0xA4, 0x82, 0xF1, 0x82, 0xBF).map {
    it.toByte()
}.toByteArray()

file.writeBytes(data)
val text = file.readText(data, Charset.forName(<small quiz for you>))
```

### Directory I/O
Directory I/O is supported by `tree://...` URI

```kotlin
val folder = FileX(context, uri, "akita") // No overhead
val child = folder.getChild("daisen") // No overhead
val neighbor = FileX(context, folder.parent, "yamagata") // No overhead
val neice = folder.getNeighbor("iwate/morioka/nakano.txt") // No overhead

if (neice.parent.mkdirs()) {
    neice.createNewFile()
    neice.renameTo(FileX(context, neice.parent, "kurokawa.json"))
}

folder.listFiles().forEach { sichouson -> // Returns FileX
    sichouson.list().forEach { // Returns Uri string
        ....
    }
}
```

## Caching

Cache is only available for the Documents URI and Tree URI

### Enabling the Cache

With constructor:
```kotlin
FileX(..., cached=true)
```
With an instance:
You can call `invalidate()` anytime you want to update Cache even when `cached` is false.
However, cached data will only be used by the FileX instance when the property `cached` is true.
Alternatively you can directly access the cache with `FileX.cache`
```kotlin
file.cached = true
file.invalidate()
```

You can update the cache with `FileX.invalidate()` or `Cache.invalidate`

Caching might impact the performance when large amounts of instances are created.
Enable cache when both of these criteria are met:
 - Frequent Access to the File properties
 - Not a lot of instances are crated at the same time

Also note that the cache of the instances created by `listFiles()` are disabled to improve performance.
