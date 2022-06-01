import React, {Component} from "react";

class Header extends Component {
    state = {};

    render() {
        return (
            <>
                <nav className="navbar navbar-static-top white-bg" role="navigation">
                    <div className="navbar-header">
                        <button className="navbar-minimalize minimalize-styl-2 btn custom-btn  btn-sm">
                            <i className="fa fa-bars"/>{" "}
                        </button>
                    </div>
                    <ul className="nav navbar-top-links navbar-right">
                        <li>
                            <a href="/search">
                                <i className="fa fa-sign-out"/> Вихід
                            </a>
                        </li>
                    </ul>
                </nav>
            </>
        );
    }
}

export default Header;
