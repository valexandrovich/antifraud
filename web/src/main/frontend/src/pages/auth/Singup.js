import React, { Component } from 'react';
import { Link } from 'react-router-dom';

class Singup extends Component {
    state = {  }
    render() { 
        return (
            <>
                <div className="fullsheet">
                    <div className="auth-form sing_up">
                        <div className="avatar"></div>
                        <h4 className="modal-title">Create an Account</h4>
                        <form>
                            <div className="form-group">
                                <input type="text"
                                    className="form-control"
                                    ref="username"
                                    name="username"
                                    placeholder="Enter Username"
                                    required
                                />
                                {/* <small className="text-danger">{this.state.username}</small> */}
                            </div>
                            <div className="form-group">
                                <input type="text"
                                    className="form-control"
                                    ref="your_name"
                                    name="your_name"
                                    placeholder="Your Name"
                                    required
                                />
                                {/* <small className="text-danger">{this.state.your_name}</small> */}
                            </div>
                            <div className="form-group">
                                <input type="email"
                                    className="form-control"
                                    ref="email"
                                    name="email"
                                    placeholder="Enter Email Address"
                                    required
                                />
                                {/* <small className="text-danger">{this.state.email}</small> */}
                            </div>
                            <div className="form-group">
                                <input type="password"
                                    className="form-control"
                                    ref="password"
                                    name="password"
                                    placeholder="password"
                                    required
                                />
                                {/* <small className="text-danger">{this.state.password}</small> */}
                            </div>
                            <div className="form-group">
                                <input type="password"
                                    className="form-control"
                                    ref="password2"
                                    name="password2"
                                    placeholder="Confirm Password"
                                    required
                                />
                                {/* <small className="text-danger">{this.state.password2}</small> */}
                            </div>
                            <button className="btn btn-primary btn-block btn-lg form-control mb-3">Singup</button>
                        </form>
                        <div className="text-center small text-dark">Already have an account? <Link to='/'>Login</Link></div>
                    </div>
                </div>
            </>
        );
    }
}
 
export default Singup;
