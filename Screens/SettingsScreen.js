import React, { Component } from 'react';
import { View, Text, StyleSheet, } from 'react-native';
import FuelEcoSettings from './../Screens/Settings';


export default class SettingsScreen extends Component {

  render() {
    return (
      <View style={styles.container}>

          <FuelEcoSettings/>
        {/* Description of the fuel cost calculation   */}
        <Text style={styles.viewText}>
            Total fuel cost calculation includes an estimate of the amount of fuel consumed in a return trip to the service station, based on the average fuel consumption of your vehicle type.
        </Text>
  
       
      </View>
    )
  };
}

const styles = StyleSheet.create({
  container: {
      margin:15,
  },
  viewText: {
      textAlignVertical:'center',
      fontSize: 15,
      paddingTop: 5,
      fontFamily: 'opensans-italic',
  },
});
