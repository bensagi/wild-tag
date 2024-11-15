import React, { useState, useEffect } from "react";
import { ArrowBigLeft, ArrowBigRight, Circle, CircleDot } from "lucide-react";
import "./image-slider.css";

const ImageSlider = ({ images }) => {
  const [imageIndex, setImageIndex] = useState(0);

  function showNextImage() {
    setImageIndex((index) => (index === images.length - 1 ? 0 : index + 1));
  }

  function showPrevImage() {
    setImageIndex((index) => (index === 0 ? images.length - 1 : index - 1));
  }

  useEffect(() => {
    const interval = setInterval(showNextImage, 2000);
    return () => clearInterval(interval);
  }, [imageIndex]);

  return (
    <section className="image-slider">
      <div className="slider-wrapper">
        <div
          className="slider-content"
          style={{ transform: `translateX(${-imageIndex * 100}%)` }}
        >
          {images.map(({ url, alt }, index) => (
            <img
              key={index}
              src={url}
              alt={alt}
              aria-hidden={imageIndex !== index}
              className="slider-image"
            />
          ))}
        </div>
      </div>
      <button onClick={showPrevImage} className="arrow left-arrow">
        <ArrowBigLeft />
      </button>
      <button onClick={showNextImage} className="arrow right-arrow">
        <ArrowBigRight />
      </button>
      <div className="dots">
        {images.map((_, index) => (
          <button
            key={index}
            className={`dot ${index === imageIndex ? "active" : ""}`}
            onClick={() => setImageIndex(index)}
          >
            {index === imageIndex ? <CircleDot /> : <Circle />}
          </button>
        ))}
      </div>
    </section>
  );
};

export default ImageSlider;
