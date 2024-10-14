import { EventEmitter, NativeModulesProxy } from "expo-modules-core";

import UhfUartReaderModule from "./UhfUartReaderModule";

import type { Subscription } from "expo-modules-core";

export function connectUhfReader(
	serialPort: string,
	baudRate: number
): boolean {
	return UhfUartReaderModule.connect(serialPort, baudRate);
}

export function listSerialPorts(): string[] {
	return UhfUartReaderModule.listSerialPorts();
}

export function listBaudRates(): number[] {
	return UhfUartReaderModule.listBaudRates();
}

export function setReaderPower(power: number): void {
	UhfUartReaderModule.setPower(power);
}

export function isUhfReaderConnected(): boolean {
	return UhfUartReaderModule.isConnected();
}

export function disconnectUhfReader(): boolean {
	return UhfUartReaderModule.disconnect();
}

const emitter = new EventEmitter(
	UhfUartReaderModule ?? NativeModulesProxy.UhfUartReader
);

export type UhfEventPayload = {
	epc: string;
};

export function addUhfListener(
	listener: (event: UhfEventPayload) => void
): Subscription {
	return emitter.addListener<UhfEventPayload>("onRead", listener);
}
