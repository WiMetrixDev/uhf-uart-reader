import { useEffect, useState } from "react";
import { Alert, StatusBar, StyleSheet, Text, View } from "react-native";

import UhfUartReader from "uhf-uart-reader";
export default function App() {
	const [connected, setConnected] = useState(false);
	const [readings, setReadings] = useState<string[]>([]);

	useEffect(() => {
		const connected = UhfUartReader.connect("/dev/ttyS5", 115200);

		if (!connected) {
			console.error("Failed to connect to UHF reader");
			Alert.alert("Failed to connect to UHF reader");
			setConnected(false);
			return;
		}

		setConnected(true);

		console.log("Connected to UHF reader");

		const subscription = UhfUartReader.addListener("onRead", ({ epc }) => {
			console.log("Read tag : ", epc);
			setReadings((prev) => [...prev, epc]);
		});

		return () => {
			console.log("Disconnecting from UHF reader");
			subscription.remove();
			UhfUartReader.disconnect();
		};
	}, []);

	return (
		<View style={styles.container}>
			<StatusBar
				animated={true}
				backgroundColor={connected ? "green" : "red"}
				barStyle="light-content"
			/>
			<Text>{connected ? "Connected" : "Disconnected"}</Text>
			<Text>Readings:</Text>
			{readings.map((reading, index) => (
				<Text key={index}>{reading}</Text>
			))}
		</View>
	);
}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		backgroundColor: "#fff",
		alignItems: "center",
		justifyContent: "center",
	},
});
