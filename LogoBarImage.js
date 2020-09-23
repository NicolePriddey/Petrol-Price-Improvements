import React, { Component } from 'react';

import { StyleSheet, View, Text, Image } from 'react-native';

export default class LogoBarImage extends Component {
  render() {
    return (
    //Display the AA logo in the navigation bar
      <View style={{ flexDirection: 'row' }}>
        <Image
          style={{ width: 50, height: 35 }}
          source={require('./assets/img/AA-logo.png')}
        />
      </View>
    );
  }
}