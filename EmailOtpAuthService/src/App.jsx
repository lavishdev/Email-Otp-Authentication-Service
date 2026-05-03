import { BrowserRouter, Routes, Route } from "react-router-dom";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Default route -> Register Page */}
        <Route path="/" element={<RegisterPage />} />
      </Routes>
    </BrowserRouter>
  );
}

