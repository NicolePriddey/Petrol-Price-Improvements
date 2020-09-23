import React, { Component } from 'react';
import { View, Text, StyleSheet, Image, TouchableOpacity, AsyncStorage, } from 'react-native';
import RNPickerSelect from 'react-native-picker-select';


export default class FuelEcoSettings extends Component {
    state = {
        vehicleType: '',
        vehicleClass: '',
        tankFill: '',
        fuelEcoId: 0,
    }
    
    componentDidMount(){
        this.getSettings('all');
    }
    
    setVehicleType(value){
        if (value == 'Hybrid'){
            this.setState({ vehicleClass: ''}, () => this.storeSettings('vehicleClass'));
        }
        this.setState({ vehicleType: value }, () => this.storeSettings('vehicleType'));        
    }

    setVehicleClass(value){
        this.setState({vehicleClass: value}, () => this.storeSettings('vehicleClass'));
    }

    setTankFill(value){
        this.setState({tankFill: value}, () => this.storeSettings('tankFill') );
    }

    setFuelEco = async() => {
        await this.getSettings('all');
        if ( this.state.vehicleType == '"Petrol"'){
            if (this.state.vehicleClass == '"Small"') {
                this.setState({fuelEcoId: 1})
            } else if (this.state.vehicleClass == '"Compact"') {
                this.setState({fuelEcoId: 2})
            } else if (this.state.vehicleClass == '"Medium"') {
                this.setState({fuelEcoId: 3})
            } else if (this.state.vehicleClass == '"Large"') {
                this.setState({fuelEcoId: 4})
            }
        } else if ( this.state.vehicleType == '"Diesel"' ){
            if (this.state.vehicleClass == '"Small"') {
                this.setState({fuelEcoId: 5})
            } else if (this.state.vehicleClass == '"Compact"') {
                this.setState({fuelEcoId: 6})
            } else if (this.state.vehicleClass == '"Medium"') {
                this.setState({fuelEcoId: 7})
            } else if (this.state.vehicleClass == '"Large"') {
                this.setState({fuelEcoId: 8})
            }
        } else if ( this.state.vehicleType == '"Hybrid"' ) {
            this.setState({fuelEcoId: 9})
        }
        await AsyncStorage.setItem("fuelEcoId", JSON.stringify(this.state.fuelEcoId));
    }

    storeSettings = async (setting) => {
        if ( setting == 'vehicleType' ) {
            await AsyncStorage.setItem("vehicleType", JSON.stringify(this.state.vehicleType));
        } else if (setting == 'vehicleClass' ){
            await AsyncStorage.setItem("vehicleClass", JSON.stringify(this.state.vehicleClass));
        } else if (setting == 'tankFill' ) {
            await AsyncStorage.setItem("tankFill", JSON.stringify(this.state.tankFill));
        }     
        
        this.setFuelEco();
    }

    getSettings = async (setting) => {
        if ( setting == 'all' ) {
            await AsyncStorage.getItem("vehicleType", (err, result) => {
                if (!err & result != null )  { this.setState({ vehicleType: result}) }});
            await AsyncStorage.getItem("vehicleClass", (err, result) => {
                if (!err & result != null ) { this.setState({ vehicleClass: result}) }});
            await AsyncStorage.getItem("fuelEcoId", (err, result) => {
                if (!err & result != null ) { this.setState({ fuelEcoId: result}) }});
            await AsyncStorage.getItem("tankFill", (err, result) => {
                if (!err & result != null ) { this.setState({ tankFill: result})  }});
        } else if ( setting == 'vehicleType') {
            await AsyncStorage.getItem("vehicleType", (err, result) => {
                if (!err & result != null ) { this.setState({ vehicleType: result}) }});
        } else if ( setting == 'vehicleClass' ) {
            await AsyncStorage.getItem("vehicleClass", (err, result) => {
                if (!err & result != null ) { this.setState({ vehicleClass: result}) }});
        } else if ( setting == 'fuelEcoId' ) { 
            await AsyncStorage.getItem("fuelEcoId", (err, result) => {
                if (!err & result != null ) { this.setState({ fuelEcoId: result}) }});
        } else if ( setting ==  'tankFill' ) {
            await AsyncStorage.getItem("tankFill", (err, result) => {
                if (!err & result != null ) { this.setState({ tankFill: result})  }});
        }
    }

    showButtons(){
        return(
        <View>
       <Text style={styles.titleLabel}>Select your vehicle class: </Text> 

            <View style={styles.buttonView}>
                {/* Small vehicle class button   */}
                <TouchableOpacity onPress={() => this.setVehicleClass('Small')}>
                    <View style={styles.vehicleClassBtn}>
                        <Image
                            style={{ width: 50, height: 28, justifyContent: 'center', alignItems: 'center' }}
                            source={require('./../assets/img/small.png')}/>
                    </View>
                </TouchableOpacity>

                {/* Compact vehicle class button   */}
                <TouchableOpacity onPress={() => this.setVehicleClass('Compact')}>
                    <View style={styles.vehicleClassBtn}>
                        <Image
                            style={{ width: 50, height: 22, justifyContent: 'center', alignItems: 'center' }}
                            source={require('./../assets/img/compact.png')}/>
                    </View>
                </TouchableOpacity>

                {/* Medium vehicle class button   */}
                <TouchableOpacity onPress={() => this.setVehicleClass('Medium')}>
                    <View style={styles.vehicleClassBtn}>
                        <Image
                            style={{ width: 50, height: 24, justifyContent: 'center', alignItems: 'center' }}
                            source={require('./../assets/img/medium.png')}/>
                    </View>
                </TouchableOpacity>

                {/* Large vehicle class button   */}
                <TouchableOpacity onPress={() => this.setVehicleClass('Large')}>
                    <View style={styles.vehicleClassBtn}>
                            <Image
                            style={{ width: 50, height: 31, justifyContent: 'center', alignItems: 'center' }}
                            source={require('./../assets/img/large.png')}/>
                    </View>
                </TouchableOpacity>
            </View>
            </View>
        )
    }
    render() {
        return (
            <View style={styles.container}>
                <Text style={styles.titleLabel}>Select your vehicle type: </Text>
                <View>
                    <RNPickerSelect
                        onValueChange={(value) => this.setVehicleType(value)}
                        items={[
                            { label: 'Petrol', value: 'Petrol' },
                            { label: 'Diesel', value: 'Diesel' },
                            { label: 'Hybrid', value: 'Hybrid' }]}/>
                </View>
                
                {this.state.vehicleType != 'Hybrid' & this.state.vehicleType != '' ? this.showButtons() : null}

                <View >
                    <Text style={styles.titleLabel}>Select your perfered tank fill amount: </Text>
                    <RNPickerSelect
                        onValueChange={(value) => this.setTankFill(value)}
                        items={[
                            { label: '20L', value: '20' },
                            { label: '30L', value: '30' },
                            { label: '40L', value: '40' },
                            { label: '50L', value: '50' }]}/>
                </View>
            </View>
        )
    };
}

const styles = StyleSheet.create({
    titleLabel: {
        textAlignVertical:'center',
        fontSize: 16,
        paddingBottom: 5,
        paddingTop: 10,
        fontFamily: 'opensans-regular'
    },
    buttonView: {
        flexDirection: 'row',
    },
    vehicleClassBtn:{
        marginLeft: 23,
        justifyContent: 'center',
        alignItems: 'center',
        height: 57,
        width: 57,  //The Width must be the same as the height
        borderRadius:400, //Then Make the Border Radius twice the size of width or Height
        backgroundColor:'#ffd520',
    }
});