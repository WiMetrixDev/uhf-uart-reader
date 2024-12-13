import { useEffect, useState } from "react";
import {
	Alert,
	Button,
	FlatList,
	StatusBar,
	StyleSheet,
	Text,
	View,
} from "react-native";

import UhfUartReader from "uhf-uart-reader";
export default function App() {
	const [connected, setConnected] = useState(false);
	const [readings, setReadings] = useState<string[]>([]);

	const connect = () => {
		const connected = UhfUartReader.connect("/dev/ttyS5");

		if (!connected) {
			console.error("Failed to connect to UHF reader");
			Alert.alert("Failed to connect to UHF reader");
			setConnected(false);
			return;
		}

		setConnected(true);

		console.log("Connected to UHF reader");
	};

	useEffect(() => {
		connect();

		const subscription = UhfUartReader.addListener("onRead", ({ epc }) => {
			console.log("Read tag : ", epc);
			setReadings((prev) => [...prev, epc]);
		});

		return () => {
			subscription.remove();
		};
	}, []);

	return (
		<View style={styles.container}>
			<Text
				style={{
					fontSize: 36,
					fontWeight: "bold",
				}}
			>
				UHF Reader
			</Text>
			<Button
				title="Connect"
				onPress={() => {
					connect();
				}}
				disabled={connected}
				color={"green"}
			/>
			<Button
				title="Disconnect"
				onPress={() => {
					UhfUartReader.disconnect();
					setConnected(false);
				}}
				disabled={!connected}
				color={"red"}
			/>
			<Button
				title="Clear Readings"
				onPress={() => {
					setReadings([]);
				}}
			/>
			<StatusBar
				animated={true}
				backgroundColor={connected ? "green" : "red"}
				barStyle="light-content"
			/>
			<Text
				style={{
					fontSize: 24,
					fontWeight: "bold",
				}}
			>
				Readings:
			</Text>
			<FlatList
				data={readings}
				renderItem={({ item, index }) => (
					<Text
						style={{
							backgroundColor: index % 2 === 0 ? "lightgray" : "white",
							padding: 5,
						}}
					>
						{item}
					</Text>
				)}
				contentContainerStyle={{
					flexGrow: 1,
					gap: 10,
				}}
			/>
		</View>
	);
}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		backgroundColor: "#fff",
		// alignItems: "center",
		justifyContent: "center",
		gap: 10,
		padding: 20,
	},
});
