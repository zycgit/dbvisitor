import React from 'react';

export default function TagYellow() {
    let styleObj = {
        backgroundColor: 'rgb(255, 186, 0)', borderRadius: '2px', color: '#fff', padding: '0.2rem', fontSize: '75%'
    }
    return (<span style={styleObj}>可选</span>);
}