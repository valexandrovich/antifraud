import React, { Component } from 'react';

class ForgotPassword extends Component {
    state = {  }
    render() { 
        return (
            <>
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">Forgot password</h5>
                        <button type="button" className="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div className="modal-body">
                        <form>
                            <div className="form-group">
                                <label htmlFor="email">Email Address</label>
                                <input className="form-control"
                                    placeholder="Enter valid email"
                                    id="email"
                                    type="email" />
                            </div>
                            <div className="text-right">
                                <button type="button" className="btn btn-danger btn-sm mr-1" data-dismiss="modal">Close</button>
                                <button type="button" className="btn btn-success btn-sm">send</button>
                            </div>
                        </form>
                    </div>
                </div>
            </>
        );
    }
}
 
export default ForgotPassword;
