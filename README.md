# UHF UART Reader

UHF RFID reader module through UART interface designed for `rk3128_box` device.

## Installation

To install run:

```bash
pnpm add uhf-uart-reader
```

or

```bash
npm install uhf-uart-reader
```

or

```bash
yarn add uhf-uart-reader
```

You will need to update your expo project to start going with a prebuilt `android` directory.

Running this command will generate the `android` directory:

```bash
pnpm expo prebuild --platform android
```

This is required because we want to modify the `build.gradle` file in this new directory to set
the following:

```gradle
android {
  ...
  defaultConfig {
    ...
    minSdkVersion = Integer.parseInt(findProperty('android.minSdkVersion') ?: '21')
        compileSdkVersion = Integer.parseInt(findProperty('android.compileSdkVersion') ?: '34')
        targetSdkVersion = Integer.parseInt(findProperty('android.targetSdkVersion') ?: '21')
  }
}
```

All this is needed because Expo SDK 50+ doesn't allow us to set the `minSdkVersion` and
`targetSdkVersion` below 31, so we need to set it manually in the `build.gradle` file.

We also need to add abiFilters in defaultConfig to support armeabi-v7a and arm64-v8a:

```gradle
android {
  ...
  defaultConfig {
    ...
    ndk { abiFilters "armeabi", "armeabi-v7a" }
  }
}
```

Once this is done, builds will work as expected.

## Usage

### `connectUhfReader`

This connects to the UHF reader on the given serial port and specified baud rate, starts reading in
the background and returns a boolean indicating if the connection was successful.

```typescript
import { connectUhfReader } from "uhf-uart-reader";

const connected = await connectUhfReader("/dev/ttyS0", 115200);
```

### `setReaderPower`

This sets the power of the UHF reader, the power should be a number between 0 and 100.

```typescript
import { setReaderPower } from "uhf-uart-reader";

setReaderPower(50);
```

### `addUhfListener`

> Note: This function should be called after `connectUhfReader` has been called.

This adds a listener to the UHF reader, the listener will be called every time a new tag is read.

```typescript
import { addUhfListener } from "uhf-uart-reader";

addUhfListener((tag) => {
	console.log(`Tag EPC: ${tag.epc}`);
});
```

This returns a function that can be called to remove the listener.

### `disconnectUhfReader`

This disconnects the UHF reader, it should be called when the reader is no longer needed.

```typescript
import { disconnectUhfReader } from "uhf-uart-reader";

disconnectUhfReader();
```

### `isUhfReaderConnected`

This returns a boolean indicating if the UHF reader is connected.

```typescript
import { isUhfReaderConnected } from "uhf-uart-reader";

const connected = isUhfReaderConnected();
```

### `listSerialPorts`

This returns a list of available serial ports on the device (the options that can be passed to
`connectUhfReader`).

```typescript
import { listSerialPorts } from "uhf-uart-reader";

const ports = await listSerialPorts();
```

### `listBaudRates`

This returns a list of available baud rates that can be passed to `connectUhfReader`. The baud
rates are hardcoded to the following values: `[9600, 19200, 38400, 57600, 115200]`.

```typescript
import { listBaudRates } from "uhf-uart-reader";

const baudRates = listBaudRates();
```
