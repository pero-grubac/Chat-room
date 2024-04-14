// NewForumRoom.js
import React from "react";
import PopUpComponent from "./PopUpComponent";
//import { useNavigate } from "react-router-dom";
import { handleUpdateForumRoom } from "../Error/ErrorMessage";
import { updateForumRoom } from "../../Services/forumRooms.Service";
import { useDispatch } from "react-redux";
import { logout } from "../Redux/slices/userSlice";

const UpdateForumRoom = ({ onClose, roomName, id }) => {
  //const navigate = useNavigate();
  const dispatch = useDispatch();

  const handleUpdate = async (values) => {
    try {
      await updateForumRoom(id, values.name);
    } catch (error) {
      handleUpdateForumRoom(error);
      dispatch(logout());
      // navigate("/login");
    }
    onClose();
  };

  return (
    <PopUpComponent
      onClose={onClose}
      roomName={roomName}
      handleSubmit={handleUpdate}
      labelName="Name"
      buttonText="Update"
    />
  );
};

export default UpdateForumRoom;
