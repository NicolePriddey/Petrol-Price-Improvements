import React, {Component} from 'react';
import { StyleSheet, Text, View, FlatList, TouchableWithoutFeedback, LayoutAnimation, ToastAndroid,
   Modal, ActivityIndicator, YellowBox, AsyncStorage, TouchableOpacity, } from 'react-native';
import DialogInput from 'react-native-dialog-input';
import { Button } from 'react-native-elements';
import RNPickerSelect from 'react-native-picker-select';
import * as Location from 'expo-location';
import * as Permissions from 'expo-permissions';
import SettingsBarImage from './../SettingsBarImage';
import FuelEcoSettings from './../Screens/Settings';

export default class Home extends Component{
  state = {
    station: [],
    location: null,
    expandedItem: false,
    expandedPrice: 0,
    isUpdatePriceDialogVisible: false,
    fuelType: '91',

    //For the Worth It function, set default states
    worthIt: [],
    apiUrl: 'http://springmavenexample-env.eba-yxucyrmu.ap-southeast-2.elasticbeanstalk.com/',
    worthItModalVisible: false,
    cheapStationName: null,
    cheapPrice: 0,
    closePrice: 0,
    showWorthItSummary: false,
    showWorthItLoad: true,

    //For the welcome screen, set default states
    firstTimeModalVisible: false,
    vehicleType: '',
    vehicleClass: '',
    tankFill: '',
  }

  constructor(props){
    super(props);

    //Ignore the warnings, it won't display these warnings in the app
    YellowBox.ignoreWarnings([
        'Warning: Failed child context type: Invalid child context `virtualizedCell.cellKey` of type `number` supplied to `CellRenderer`, expected `string`',
        'Warning: Each child in a list should have a unique "key" prop'
    ])
  }

  componentDidMount(){
    this.getInitalData();
    
    this.props.navigation.setParams({
      vehicleType: this.state.vehicleType,
    });

    //AsyncStorage.clear();
    this.checkFirstTime();
    this.getSettings();
  }

  getInitalData = async () => {
    await this.getLocationData();
    this.getStationData();
  }

  checkFirstTime(){
    AsyncStorage.getItem("firstTime", (err, result) => {
      if (!err & result === null) { 
        this.setftModalVisible(true); 
        AsyncStorage.setItem("firstTime", JSON.stringify("false"));}
    });    
  }

  //Get the items selected in the settings page
  getSettings = async () => {
    await AsyncStorage.getItem("vehicleType", (err, result) => {
        if (!err & result != null )  { console.log("Setting: ", result); this.setState({ vehicleType: result}) }});
    await AsyncStorage.getItem("vehicleClass", (err, result) => {
        if (!err & result != null ) { console.log("Setting: ", result); this.setState({ vehicleClass: result}) }});
    await AsyncStorage.getItem("fuelEcoId", (err, result) => {
        if (!err & result != null ) { console.log("Setting: ", result); this.setState({ fuelEcoId: result}) }});
    await AsyncStorage.getItem("tankFill", (err, result) => {
        if (!err & result != null ) { console.log("Setting: ", result); this.setState({ tankFill: result})  }});
  }
  
  
  setftModalVisible(visible) {
    this.setState({ firstTimeModalVisible: visible });
  }

  //Display the home page title and set the settings navigation to navigate to the Settings page
  static navigationOptions = ({navigation}) => {
    const { params = {} } = navigation.state
    return {
      headerTitle: 'Nearby prices for ' + params.fuelType,
      headerRight: ()=> 
        <TouchableOpacity onPress={() => navigation.navigate('SettingsScreen')}>
          <SettingsBarImage />
        </TouchableOpacity>
    }
  }

 //Location function
 getLocationData = async () => {
   let { status } = await Permissions.askAsync(Permissions.LOCATION);
   if (status !== 'granted') {
     const locationErrorMessage = 'Permission to access location was denied';
     ToastAndroid.show("Error: " + locationErrorMessage, ToastAndroid.SHORT);
   }

   let location = await Location.getCurrentPositionAsync({accuracy:Location.Accuracy.BestForNavigation});
   const { latitude , longitude } = location.coords
   this.setState({ location: {latitude, longitude}});
   this.getStationData();
 };

  getStationData = async () => {
    console.log(this.state.apiUrl + 'view/' + this.state.fuelType + '/8/-41.215136/174.800471')
    const response = fetch(this.state.apiUrl + 'view/' + this.state.fuelType + '/8/' + this.state.location.latitude + '/' + this.state.location.longitude );
    const json = await (await response).json();
    this.setState({ station: json});
  }

  setWorthItModalVisible = (visible) => {
    this.setState({ worthItModalVisible: visible});
  }

  worthItPrep = async () => {
    this.getLocationData();
    await this.getSettings();
    this.setState(prevState => ({tankFill: prevState.tankFill.replace(new RegExp("\"", "g"), "")}));
    this.setState({ worthIt: [], showWorthItSummary: false, showWorthItLoad: true, });
  }

  //Worth It function
  getWorthIt = async () => {
    await this.worthItPrep();
    const response = fetch(this.state.apiUrl + 'worthit/' + this.state.location.latitude + '/' + this.state.location.longitude + '/' + this.state.fuelType + '/' + this.state.fuelEcoId + '/' + this.state.tankFill);
    const json = await (await response).json();
    this.setState({ worthIt: json});
    this.setState({ 
      worthIt: json,
      cheapStationName: json[0].stationName,
      cheapPrice: json[0].cost,
      closePrice: json[1].cost,
      showWorthItSummary: true,
      showWorthItLoad: false,
    });
    this.setState({ showWorthItSummary: json[1].stationName != null ? true : false })
    console.log(this.state.cheapStationName);
  }

  renderSeparator = () => {
    return (<View style={{ height: 1, backgroundColor: "#ffd520", }} />);
  };

  onChangeLayout = stationID => {
    LayoutAnimation.configureNext
    (LayoutAnimation.Presets.easeInEaseOut);
    const array = [...this.state.station];
    array.map((value, placeIndex) =>
        placeIndex === stationID
            ? (array[placeIndex][value] = !array[placeIndex][value])
            : (array[placeIndex][value] = false)
        );
    this.setState({ expandedItem: stationID });
  };

  sendInput = inputText =>{
    this.setState({ isUpdatePriceDialogVisible: false })
    this.updatePrice(inputText);
    this.getLocationData();
    this.setState({ expandedItem: false })
  }

  //Update price function and display toast messages
  updatePrice = async (price) => {
    const response = fetch(this.state.apiUrl + '/update/' + this.state.fuelType + '/' + this.state.expandedItem + '/' + price);
    const result = await (await response).text();
    (result === true) ? this.showUpdateMessageToast("Successfully updated") : this.showUpdateMessageToast("Error. Did not update");
  }
  
  showUpdateMessageToast = (successful) => {
      ToastAndroid.show(successful, ToastAndroid.SHORT);
  }

  //Display the Worth It function
  renderItem = (data,index) => 
  <View>
    <Text style={styles.worthItlabel}>{`${(index===0) ? 'Cheapest' : 'Closest'} station: `}</Text>
    {
      data.item.stationName != null ? 
      <View>
        <Text style={styles.worthItText}>{data.item.stationName}</Text>
        <Text style={styles.worthItlabel}>{`Travel cost + ${this.state.tankFill}L Fill: `}</Text>
        <Text style={styles.worthItText}>{`$${data.item.cost}`}</Text>
        <Text style={styles.worthItlabel}>{`Travel Time: `}</Text>
        <Text style={styles.worthItText}>{`${(data.item.timeS % 3600 / 60).toFixed(2)}min`}</Text>
      </View>
      :
      <Text style={styles.worthItText}>{`There is no price information in 8km from your location`}</Text>    
    }  
    </View>

  render () {
    return(
      <View style={styles.container} >
        
        <View>
          <RNPickerSelect
            onValueChange={(value) => this.setState({fuelType: value},
              console.log(value),
              this.props.navigation.setParams({fuelType: value}),
              this.getLocationData())}
            items={[
                { label: '91', value: '91' },
                { label: '95', value: '95' },
                { label: '98', value: '98' },
                { label: 'Diesel', value: 'Diesel' }]}/>
        </View>

        {/* display list */}          
        <FlatList
          style={styles.list}
          keyExtractor={(x,i) => i}
          data={this.state.station}
          ItemSeparatorComponent={this.renderSeparator}

          renderItem={({item, index}) => (
            <View>

              <TouchableWithoutFeedback onPress={() => {
                (this.state.expandedItem === item.stationID) ? this.setState({ expandedItem: false}) : this.onChangeLayout(item.stationID);
                {this.setState({ expandedPrice: item.price})}}}
                style={{
                  height: 50,
                  justifyContent: 'center',
                  alignItems: 'center',
                  backgroundColor: '#ffecb3'}}>
                <Text style={styles.item}>
                  {`$${(Math.round(item.price * 100) / 100).toFixed(2)}  |  ${item.stationName}` }
                </Text>
              </TouchableWithoutFeedback>

              <View
                style={{
                    height: (this.state.expandedItem === item.stationID) ? null : 0,
                    overflow: 'hidden',
                    paddingHorizontal:(this.state.expandedItem === item.stationID) ? 10 : 0,
                    paddingBottom:(this.state.expandedItem === item.stationID) ? 20 : 0 }}>
                <Text style={{...styles.expandedItem}}> {item.address}</Text>

                <Button 
                  titleStyle={{ color: 'black' }} 
                  buttonStyle={{backgroundColor: '#ffd520'}} 
                  title='Update Price' 
                  onPress={() => this.setState({ isUpdatePriceDialogVisible: true})}/>

                <Text style={{...styles.lastUpdate}}> {`Last updated: ${item.lastUpdate.slice(0, item.lastUpdate.indexOf(","))}`}</Text>
              </View>

            </View>
          )}/>

        {/* worth it button */}
        <View style={styles.button}>
          <Button titleStyle={{ color: 'black' }} buttonStyle={{backgroundColor: '#85bb65'}}
              title="Worth It?"
              onPress = {() => {this.setWorthItModalVisible(true); this.getWorthIt(); }}
            />
        </View>

        {/* update price dialog   */}
        <DialogInput isDialogVisible={this.state.isUpdatePriceDialogVisible}
          title={"Current price: $ " + (Math.round(this.state.expandedPrice * 100) / 100).toFixed(2)}
          message={"Enter new price"}
          hintInput ={"NEW PRICE"}
          textInputProps={{keyboardType:'numeric'}}
          submitInput={ (inputText) => {this.sendInput(inputText)} }
          closeDialog={ () => {this.setState({ isUpdatePriceDialogVisible: false })}}>
        </DialogInput>

        {/* worth it modal */} 
        <Modal
          animationType="slide"
          transparent={false}
          visible = {this.state.worthItModalVisible}
          onRequestClose={() => this.closeModal()}>
          <View style={styles.centeredView}>
            <View style={styles.worthItModal}>

              <Text style = {styles.worthItTitleText}>Worth It: {this.state.fuelType}</Text>
              {this.state.showWorthItLoad ? <ActivityIndicator size="large" color="#ffd520" style={styles.worthItLoading}/> : null }
              <View style={styles.container}>
                <FlatList
                  data= {this.state.worthIt}
                  ItemSeparatorComponent = {this.renderSeparator}
                  renderItem= {item=> this.renderItem(item, item.index)}
                  keyExtractor= {item=>item.stationName}/>
              </View>

              <View style={styles.worthItSummary}>
                {this.state.showWorthItSummary ? <Text style={styles.worthItSummaryText}>You will save ${(this.state.closePrice - this.state.cheapPrice).toFixed(2)} by going to {this.state.cheapStationName}</Text> : null}
              </View>

              <Button
                  title="Close"
                  onPress = {() => this.setWorthItModalVisible(false)}
                  titleStyle={{ color: 'black' }}
                  buttonStyle={{backgroundColor: '#ffd520'}}/>

            </View>
          </View>
        </Modal>
        
        {/* first time modal */}
        <Modal
            animationType={"slide"}
            transparent={true}
            visible={this.state.firstTimeModalVisible}
            onRequestClose={() => {
              alert("Modal has been closed.");}}>

          <View style={styles.firstTimecontainer}>
            <View style={styles.ftModal}>

              <View style={styles.ftIntroText} >
                <Text style={styles.ftIntroText}>Welcome</Text>
                <Text style={styles.ftIntroText}>We need some information from you to help us give you more accurate information.</Text>
              </View>

              <View>
                <FuelEcoSettings/>
              </View>
        
              <View>
                  <Text style={styles.ftLabelText}>This data will be used to get the average fuel consumption for your car.</Text>
                  <Text style={styles.ftLabelText}>Your perfered tank fill amount will be used to give you a more relevant calculation</Text>
              </View>
              
            </View>
            <View style={styles.ftButton}>
                <Button
                  title="Close"
                  onPress = {() => this.setftModalVisible(!this.state.firstTimeModalVisible)}
                  titleStyle={{ color: 'black' }}
                  buttonStyle={{backgroundColor: '#ffd520'}}/>
             </View>
            </View>
          </Modal>    

        </View>
    )};}


const styles = StyleSheet.create({
  container: {
    //marginTop:15,
    flex: 1,
  },
  item: {
    padding:20,
    backgroundColor: 'white',
    fontSize: 18,
    fontFamily: 'opensans-regular',
  },
  expandedItem: {
    fontSize: 15,
    paddingBottom: 5,
    fontFamily: 'opensans-light',
  },
  lastUpdate: {
    fontSize: 12,
    textAlign: 'right'
  },
  list:{
    paddingVertical: 4,
    margin: 5,
    backgroundColor: "#fff",
  },
  worthItTitleText: {
    width:'100%',
    textAlignVertical:'center',
    fontSize: 25,
    fontFamily: 'opensans-bold',
    padding: 10,
  },
  worthItText: {
    width:'100%',
    textAlignVertical:'center',
    fontSize: 15,
    paddingBottom:10,
    fontFamily: 'opensans-italic',
  },
  worthItlabel: {
    textAlignVertical:'center',
    fontSize: 20,
    paddingBottom:5,
    fontFamily: 'opensans-regular'
  },
  centeredView: {
    flex: 1,
    alignItems: "center",
    marginTop: 10,
    justifyContent: "center",
  },
  worthItModal: {
    margin: 10,
    backgroundColor: "white",
    alignItems: "center",
  },
  button: {
    padding: 10,
    marginBottom: 1,
  },
  worthItSummary: {
    marginBottom: 15,
    padding: 5,
  },
  worthItSummaryText: {
    backgroundColor: '#85bb65',
    fontSize: 20,
    fontFamily: 'opensans-regular',
  }, 
  firstTimecontainer: {
    marginTop:15,
    flex: 1,
    margin: 10,
    backgroundColor: "white",
  },
  ftLabelText: {
    fontSize: 16,
    fontFamily: 'opensans-italic',
    paddingTop: 20,
  },
  ftIntroText: {
    fontSize: 18,
    fontFamily: 'opensans-regular',
    alignItems: "center",
    paddingBottom: 5,
  },
  ftModal: {
    margin: 10,
  }, 
  ftButton: {
    paddingTop: 10,
    marginBottom: 1,
  },
  worthItLoading: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    alignItems: 'center',
    justifyContent: 'center'
  }
});
