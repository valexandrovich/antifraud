import React, {Component} from "react";
import Nav from "../../components/Nav";
import Header from "../../components/Header";

class Dashboard extends Component {
    state = {};

    render() {
        return (
            <>
                <div className="nav_bg_color" id="wrapper">
                    <Nav/>
                    <div id="page-wrapper" className="gray-bg">
                        <div className="row border-bottom">
                            <Header/>
                        </div>
                    </div>
                </div>
            </>
        );
    }
}

export default Dashboard;
