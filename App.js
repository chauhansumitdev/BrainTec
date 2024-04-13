import React, { useState, useEffect } from 'react';
import { StatusBar, PermissionsAndroid, Alert, StyleSheet, Text, View, Button } from 'react-native';
import { Bluetooth, Speech, Permissions, Contacts, Linking } from 'expo';

export default function App() {
  const [bluetoothConnected, setBluetoothConnected] = useState(false);

  useEffect(() => {
    // Check and request necessary permissions
    async function checkPermissions() {
      try {
        // Request Bluetooth permission
        const bluetoothPermission = await Permissions.askAsync(Permissions.AUDIO_RECORDING);
        if (bluetoothPermission.status !== 'granted') {
          Alert.alert('Bluetooth Permission', 'Bluetooth permission denied. The app may not work correctly.');
        }
  
        // Add event listener for Bluetooth connection
        Bluetooth.addListener('connectionStatusChanged', ({ connected }) => {
          setBluetoothConnected(connected);
          if (!connected) {
            sendEmergencyMessage();
            callEmergencyContact();
          }
        });
  
        // Check initial Bluetooth connection status
        const connected = await Bluetooth.isConnectedAsync();
        setBluetoothConnected(connected);
  
      } catch (error) {
        console.error('Error checking permissions:', error);
      }
    }
  
    checkPermissions();
  
    // Clean up event listeners when component unmounts
    return () => {
      if (Bluetooth) {
        Bluetooth.removeAllListeners();
      }
    };
  
  }, []);
  

  // Function to start speech recognition
  const startSpeechRecognition = async () => {
    try {
      const { status } = await Permissions.askAsync(Permissions.AUDIO_RECORDING);
      if (status !== 'granted') {
        Alert.alert('Speech Recognition Permission', 'Speech recognition permission denied. The app may not work correctly.');
        return;
      }

      const result = await Speech.startListeningAsync();
      if (result.error) {
        Alert.alert('Speech Recognition Error', 'Failed to transcribe speech.');
        return;
      }

      // Use Google API to search for answers based on the transcribed speech (not implemented here)
      console.log('Transcribed speech:', result);
    } catch (error) {
      console.error('Error starting speech recognition:', error);
    }
  };

  // Function to send an emergency message
  const sendEmergencyMessage = async () => {
    try {
      const emergencyContact = 'YOUR_EMERGENCY_CONTACT_NUMBER';
      const message = 'Emergency: Driver is in trouble!';
      await Contacts.sendMessageAsync([emergencyContact], message);
      console.log('Emergency message sent successfully');
    } catch (error) {
      console.error('Error sending emergency message:', error);
    }
  };

  // Function to call emergency contact
  const callEmergencyContact = async () => {
    try {
      const emergencyContact = 'YOUR_EMERGENCY_CONTACT_NUMBER';
      await Linking.openURL(`tel:${emergencyContact}`);
    } catch (error) {
      console.error('Error calling emergency contact:', error);
    }
  };

  return (
    <>
      <StatusBar style="auto" />
      <View style={styles.container}>
        <Text>BrainTec</Text>
        {bluetoothConnected ? (
          <Text style={styles.connected}>Bluetooth headset connected</Text>
        ) : (
          <Text style={styles.disconnected}>Bluetooth headset disconnected</Text>
        )}
        <Button title="Use Google Assistant" onPress={startSpeechRecognition} />
      </View>
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  connected: {
    color: 'green',
    marginTop: 10,
  },
  disconnected: {
    color: 'red',
    marginTop: 10,
  },
});
