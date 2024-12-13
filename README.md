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

This module contains an Expo Config Plugin that will automatically add the changes to gradle
configuration files when the module is installed. If you are not using Expo, you will need to
manually add the changes to the gradle configuration files.

### Expo Config Plugin

Add `uhf-uart-reader` to the `plugins` in `app.json` or `app.config.ts`:

```json
{
	"expo": {
		"plugins": ["uhf-uart-reader"]
	}
}
```

### Manual Installation

### android/build.gradle

Update the minSdkVersion and targetSdkVersion to 21:

### android/app/build.gradle

Add the following:

```gradle
...
android {
  ...
  defaultConfig {
    ...
    ndk {
      abiFilters "armeabi-v7a", "armeabi"
    }
  }
}
...
```

## Usage

### `connect`

This connects to the UHF reader on the given serial port and specified baud rate, starts reading in
the background and returns a boolean indicating if the connection was successful.

```typescript
import UhfUartReader from "uhf-uart-reader";

const connected = UhfUartReader.connect("/dev/ttyS0");
```

### `setPower`

This sets the power of the UHF reader, the power should be a number between 0 and 100.

```typescript
import UhfUartReader from "uhf-uart-reader";

UhfUartReader.setPower(50);
```

### `addUhfListener`

> Note: This function should be called after `connectUhfReader` has been called.

This adds a listener to the UHF reader, the listener will be called every time a new tag is read.

```typescript
import UhfUartReader from "uhf-uart-reader";

UhfUartReader.addListener("onRead", (tag) => {
	console.log(`Tag EPC: ${tag.epc}`);
});
```

This returns a function that can be called to remove the listener.

### `disconnect`

This disconnects the UHF reader, it should be called when the reader is no longer needed.

```typescript
import UhfUartReader from "uhf-uart-reader";

UhfUartReader.disconnect();
```

### `isConnected`

This returns a boolean indicating if the UHF reader is connected.

```typescript
import UhfUartReader from "uhf-uart-reader";

const connected = UhfUartReader.isConnected();
```

### `listSerialPorts`

This returns a list of available serial ports on the device (the options that can be passed to
`connectUhfReader`).

```typescript
import UhfUartReader from "uhf-uart-reader";

const ports = UhfUartReader.listSerialPorts();
```
