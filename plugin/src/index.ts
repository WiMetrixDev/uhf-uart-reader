import { ExpoConfig } from "@expo/config-types";
import {
	withAppBuildGradle,
	withProjectBuildGradle,
} from "expo/config-plugins";

/**
 * A config plugin to modify the `app/build.gradle` file.
 * - Adds `ndk.abiFilters` to `defaultConfig`
 * - Updates `minSdkVersion` and `targetSdkVersion` to `21` in `defaultConfig`
 */
const withNdkAbiFilters = (
	config: ExpoConfig,
	abiFilters = ["armeabi-v7a", "armeabi"],
	minSdkVersion = 21,
	targetSdkVersion = 21
) => {
	let updatedConfig = withAppBuildGradle(config, (config) => {
		if (config.modResults.language === "groovy") {
			const defaultConfigPattern = /defaultConfig\s*\{([\s\S]*?)\}/g;
			const defaultConfigMatch = defaultConfigPattern.exec(
				config.modResults.contents
			)?.[0];

			if (defaultConfigMatch) {
				let updatedDefaultConfig = defaultConfigMatch;

				// Check if abiFilters are already defined
				const abiFiltersPattern = /ndk\s*\{\s*abiFilters\s+([\s\S]*?)\}/g;
				let abiFiltersMatch = abiFiltersPattern.exec(defaultConfigMatch);

				if (!abiFiltersMatch) {
					// Add the abiFilters inside the defaultConfig
					const ndkConfig = `
						ndk {
							abiFilters ${abiFilters.map((abi) => `"${abi}"`).join(", ")}
						}
					`;

					// Inject the ndk.abiFilters inside the defaultConfig block
					updatedDefaultConfig = defaultConfigMatch.replace(
						/defaultConfig\s*\{/,
						`defaultConfig {\n${ndkConfig}\n`
					);
				}

				// Update minSdkVersion and targetSdkVersion to 21
				updatedDefaultConfig = updatedDefaultConfig
					.replace(/minSdkVersion\s+[\w\.]+/, `minSdkVersion ${minSdkVersion}`)
					.replace(
						/targetSdkVersion\s+[\w\.]+/,
						`targetSdkVersion ${targetSdkVersion}`
					);

				config.modResults.contents = config.modResults.contents.replace(
					defaultConfigMatch,
					updatedDefaultConfig
				);
			}
		}
		return config;
	});

	updatedConfig = withProjectBuildGradle(updatedConfig, (config) => {
		if (config.modResults.language === "groovy") {
			// Update minSdkVersion and targetSdkVersion to 21
			config.modResults.contents = config.modResults.contents
				.replace(/minSdkVersion.*/, `minSdkVersion = ${minSdkVersion}`)
				.replace(
					/targetSdkVersion.*/,
					`targetSdkVersion = ${targetSdkVersion}`
				);
		}
		return config;
	});

	return updatedConfig;
};

export default withNdkAbiFilters;
