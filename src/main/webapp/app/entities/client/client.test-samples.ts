import { IClient, NewClient } from './client.model';

export const sampleWithRequiredData: IClient = {
  id: 71655,
  firstName: 'Esta',
  lastName: 'Stamm',
  email: 'Hosea.Volkman47@gmail.com',
  phone: '981.379.2056 x899',
  address: 'Corners',
};

export const sampleWithPartialData: IClient = {
  id: 48240,
  firstName: 'Destini',
  lastName: 'Dach',
  email: 'Kellen_Walker71@yahoo.com',
  phone: '1-227-820-5074',
  address: 'reinvent PNG seamless',
};

export const sampleWithFullData: IClient = {
  id: 41109,
  firstName: 'Casimer',
  lastName: 'Bode',
  email: 'Percy.Abernathy17@hotmail.com',
  phone: '317.773.2228',
  address: 'Brand',
};

export const sampleWithNewData: NewClient = {
  firstName: 'Kameron',
  lastName: 'Rau',
  email: 'Scarlett_VonRueden@gmail.com',
  phone: '427.517.8525 x29682',
  address: 'Metrics',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
