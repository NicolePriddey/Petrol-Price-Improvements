import React from 'react';
import { createAppContainer } from "react-navigation";
import { createStackNavigator } from 'react-navigation-stack';
import * as Font from 'expo-font';
import { AppLoading } from 'expo';
import Home from './Screens/Home';
import SettingsScreen from './Screens/SettingsScreen';
import LogoBarImage from './LogoBarImage';

//Function to retrieve and loads custom fonts
const fetchFonts = () => {
        return Font.loadAsync({
        'opensans-bold': require('./assets/fonts/OpenSans-Bold.ttf'),
        'opensans-italic': require('./assets/fonts/OpenSans-Italic.ttf'),
        'opensans-regular': require('./assets/fonts/OpenSans-Regular.ttf'),
        'opensans-light': require('./assets/fonts/OpenSans-Light.ttf')
        });
};

export default class App extends React.Component {
  state = {
      fontsLoaded: false,
  }

  render() {
    if (!this.state.fontsLoaded){
      return(
        <AppLoading
          startAsync={fetchFonts}
          onFinish={() => this.setState({fontsLoaded: true})}/>
      )
    }

    return <AppContainer />;
  }
}

//Navigation Bar
const AppNavigator = createStackNavigator(
{
  Home: {
    screen: Home,
    navigationOptions: {
        title: 'Nearby prices for ',
        headerLeft: ()=> <LogoBarImage />,
        headerTitleAlign: 'center',
        headerStyle: {
            backgroundColor: '#ffd520',
        },
        headerTitleStyle:{
            fontSize: 25,
            fontFamily: 'opensans-bold',
        }
    },
  },
  SettingsScreen: {
    screen: SettingsScreen,
    navigationOptions: {
        title: 'Settings',
        headerTitleAlign: 'center',
        headerStyle: {
            backgroundColor: '#ffd520',
        },
        headerTitleStyle:{
            fontSize: 25,
            fontFamily: 'opensans-bold',
        }
    },
  },
},

    {
        initialRouteName: "Home"
    }

);

const AppContainer = createAppContainer(AppNavigator);
