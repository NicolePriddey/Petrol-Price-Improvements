//Icons made by <a href="https://www.flaticon.com/authors/freepik" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>

import React, { Component } from 'react';

import { StyleSheet, View, Text, Image, TouchableOpacity } from 'react-native';

export default class SettingsBarImage extends Component {
  render() {
    return (
    //Display the settings cog icon in the navigation bar
      <View style={{ flexDirection: 'row' }}>
          <Image
            style={{ width: 32, height: 32, marginRight: 10 }}
            source={require('./assets/img/interface.png')}
          />
      </View>
    );
  }
}