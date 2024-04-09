import Error from "./Error";
const Error500 = () => {
  return (
    <Error
      status="500"
      title="500"
      subTitle="Sorry, you are not authorized to access this page."
      />
  );
};
export default Error500;
