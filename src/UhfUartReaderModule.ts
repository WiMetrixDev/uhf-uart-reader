import { NativeModule, requireNativeModule } from "expo";

export type UhfEventPayload = {
	epc: string;
};

export type UhfUartReaderEvents = {
	onRead: (params: UhfEventPayload) => void;
};

declare class UhfUartReader extends NativeModule<UhfUartReaderEvents> {
	connect(serialPort: string, baudRate: number): boolean;
	listSerialPorts(): string[];
	listBaudRates(): number[];
	setPower(power: number): void;
	isConnected(): boolean;
	disconnect(): boolean;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<UhfUartReader>("UhfUartReader");
